package com.darkere.crashutils;

import com.darkere.crashutils.DataStructures.TileEntityData;
import com.darkere.crashutils.Network.*;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class WorldUtils {

    public static List<ServerLevel> getWorldsFromDimensionArgument(CommandContext<CommandSourceStack> context) {
        ServerLevel world = null;
        try {
            world = DimensionArgument.getDimension(context, "dim");
        } catch (IllegalArgumentException | CommandSyntaxException e) {
            //NO OP
        }
        List<ServerLevel> worlds = new ArrayList<>();
        if (world == null) {
            context.getSource().getServer().getAllLevels().forEach(worlds::add);
        } else {
            worlds.add(world);
        }
        return worlds;
    }

    public static void teleportPlayer(Player player, Level startWorld, Level destWorld, BlockPos newPos) {
        if (player.level.isClientSide) {
            Network.sendToServer(new TeleportMessage(startWorld.dimension(), destWorld.dimension(), newPos));
        }
        if (newPos.getY() == 0) {
            ChunkAccess chunk = destWorld.getChunkAt(newPos);
            int y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, newPos.getX(), newPos.getZ());
            newPos = new BlockPos(newPos.getX(), y, newPos.getZ());
        }
        if (startWorld != destWorld) {
            BlockPos finalNewPos = newPos;
            player.changeDimension((ServerLevel) destWorld, new ITeleporter() {
                @Override
                public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                    Entity entity1 = repositionEntity.apply(false);
                    entity1.teleportTo(finalNewPos.getX(), finalNewPos.getY() + 1, finalNewPos.getZ());
                    return entity1;
                }
            });
        } else {
            player.teleportTo(newPos.getX(), newPos.getY() + 1, newPos.getZ());
        }
    }

    public static boolean applyToPlayer(String playerName, MinecraftServer server, Consumer<ServerPlayer> consumer) {
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            Optional<GameProfile> profile = server.getProfileCache().get(playerName);
            if (profile.isEmpty()) {
                return false;
            }

            FakePlayer fakePlayer = new CustomFakePlayer(server.getLevel(Level.OVERWORLD), profile.get());
            CompoundTag nbt = server.playerDataStorage.load(fakePlayer);
            if (nbt == null) return false;
            fakePlayer.load(nbt);
            consumer.accept(fakePlayer);
            server.playerDataStorage.save(fakePlayer);

        } else {
            consumer.accept(player);
        }

        return true;
    }

    public static BlockPos getChunkCenter(ChunkPos pos) {
        int x = pos.getMinBlockX() + 8;
        int z = pos.getMinBlockZ() + 8;
        return new BlockPos(x, 0, z);
    }

    public static void removeEntity(Level world, UUID id) {
        if (NetworkTools.returnOnNull(world, id)) return;
        if (world.isClientSide) {
            Network.sendToServer(new RemoveEntityMessage(world.dimension(), id, false, false));
            return;
        }
        Entity e = ((ServerLevel) world).getEntity(id);
        if (e == null) return;
        e.remove(Entity.RemovalReason.DISCARDED);
    }

    public static void removeEntityType(Level world, ResourceLocation rl, boolean force) {
        if (NetworkTools.returnOnNull(world, rl)) return;
        List<Runnable> runnables = new ArrayList<>();
        if (world.isClientSide) {
            Network.sendToServer(new RemoveEntitiesMessage(world.dimension(), rl, null, false, force));
            return;
        }
        ((ServerLevel) world).getEntities().getAll().forEach(entity ->{
            var rl2 = ForgeRegistries.ENTITIES.getKey(entity.getType());
            if(!Objects.equals(rl2, rl))
                return;
            runnables.add(()-> entity.remove(Entity.RemovalReason.DISCARDED));
        });

        runnables.forEach(Runnable::run);
    }

    public static void removeEntitiesInChunk(Level world, ChunkPos pos, ResourceLocation rl, boolean force) {
        if (NetworkTools.returnOnNull(world, pos, rl)) return;
        if (world.isClientSide) {
            Network.sendToServer(new RemoveEntitiesMessage(world.dimension(), rl, pos, false, force));
            return;
        }
        Vec3 start = new Vec3(pos.getMinBlockX(), 0, pos.getMinBlockZ());
        Vec3 end = new Vec3(pos.getMaxBlockX(), 255, pos.getMaxBlockZ());
        world.getEntities((Entity) null, new AABB(start, end), entity -> Objects.equals(ForgeRegistries.ENTITIES.getKey(entity.getType()), rl)).forEach(e -> e.remove(Entity.RemovalReason.DISCARDED));
    }

    public static void removeTileEntity(Level world, UUID id, boolean force) {
        if (NetworkTools.returnOnNull(world, id)) return;
        if (world.isClientSide) {
            Network.sendToServer(new RemoveEntityMessage(world.dimension(), id, true, force));
            return;
        }
        CrashUtils.runNextTick((wld)-> {
            if (force) {
                world.removeBlockEntity(TileEntityData.TEID.get(id).pos);
                world.removeBlock(TileEntityData.TEID.get(id).pos, false);
            } else {
                world.removeBlockEntity(TileEntityData.TEID.get(id).pos);
            }
        });
    }

    public static void removeTileEntityType(Level world, ResourceLocation rl, boolean force) {
        if (NetworkTools.returnOnNull(world, rl)) return;
        if (world.isClientSide) {
            Network.sendToServer(new RemoveEntitiesMessage(world.dimension(), rl, null, true, force));
            return;
        }
        for (TickingBlockEntity te : world.blockEntityTickers) {
            if (Objects.equals(te.getType(), rl.toString())) {
                CrashUtils.runNextTick((wld)->{
                    if (force) {
                        world.removeBlockEntity(te.getPos());
                        world.removeBlock(te.getPos(), false);
                    } else {
                        world.removeBlockEntity(te.getPos());
                    }
                });
            }
        }
    }

    public static void removeTileEntitiesInChunk(Level world, ChunkPos pos, ResourceLocation rl, boolean force) {
        if (NetworkTools.returnOnNull(world, pos, rl)) return;
        if (world.isClientSide) {
            Network.sendToServer(new RemoveEntitiesMessage(world.dimension(), rl, pos, true, force));
            return;
        }
        Vec3 start = new Vec3(pos.getMinBlockX(), 0, pos.getMinBlockZ());
        Vec3 end = new Vec3(pos.getMaxBlockX(), 255, pos.getMaxBlockZ());

        world.blockEntityTickers.stream().filter(te -> Objects.equals(te.getType(), rl.toString()) && new AABB(start, end).contains(Vec3.atCenterOf(te.getPos()))).forEach(te -> {
            CrashUtils.runNextTick((wld) -> {
                if (force) {
                    world.removeBlockEntity(te.getPos());
                    world.removeBlock(te.getPos(), false);
                } else {
                    world.removeBlockEntity(te.getPos());
                }
            });
        });
    }
}
