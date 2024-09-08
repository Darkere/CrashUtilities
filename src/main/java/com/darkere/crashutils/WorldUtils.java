package com.darkere.crashutils;

import com.darkere.crashutils.DataStructures.TileEntityData;
import com.darkere.crashutils.Network.*;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

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
        if (player.getCommandSenderWorld().isClientSide()) {
            Network.sendToServer(new TeleportMessage(startWorld.dimension(), destWorld.dimension(), newPos));
        }
        if (newPos.getY() == 0) {
            ChunkAccess chunk = destWorld.getChunkAt(newPos);
            int y = chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, newPos.getX(), newPos.getZ());
            newPos = new BlockPos(newPos.getX(), y, newPos.getZ());
        }
        player.teleportTo((ServerLevel) destWorld,newPos.getX() +1 ,newPos.getY(),newPos.getZ(),Set.of(), player.getYRot(), player.getXRot());
    }

    public static boolean applyToPlayer(String playerName, MinecraftServer server, Consumer<ServerPlayer> consumer) {
        ServerPlayer player = server.getPlayerList().getPlayerByName(playerName);
        if (player == null) {
            Optional<GameProfile> profile = server.getProfileCache().get(playerName);
            if (profile.isEmpty()) {
                return false;
            }

            FakePlayer fakePlayer = new CustomFakePlayer(server.getLevel(Level.OVERWORLD), profile.get());
            Optional<CompoundTag> nbt = server.playerDataStorage.load(fakePlayer);
            if (nbt.isEmpty()) return false;
            fakePlayer.load(nbt.get());
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
            Network.sendToServer(new RemoveEntitiesMessage(world.dimension(), rl, ChunkPos.ZERO, false, force,true));
            return;
        }
        ((ServerLevel) world).getEntities().getAll().forEach(entity -> {

            var rl2 = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            if (!Objects.equals(rl2, rl))
                return;
            runnables.add(() -> entity.remove(Entity.RemovalReason.DISCARDED));
        });

        runnables.forEach(Runnable::run);
    }

    public static void removeEntitiesInChunk(Level world, ChunkPos pos, ResourceLocation rl, boolean force) {
        if (NetworkTools.returnOnNull(world, pos, rl)) return;
        if (world.isClientSide) {
            Network.sendToServer(new RemoveEntitiesMessage(world.dimension(), rl, pos, false, force, false));
            return;
        }
        Vec3 start = new Vec3(pos.getMinBlockX(), 0, pos.getMinBlockZ());
        Vec3 end = new Vec3(pos.getMaxBlockX(), 255, pos.getMaxBlockZ());
        world.getEntities((Entity) null, new AABB(start, end), entity -> Objects.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()), rl)).forEach(e -> e.remove(Entity.RemovalReason.DISCARDED));
    }

    public static void removeTileEntity(Level world, UUID id, boolean force) {
        if (NetworkTools.returnOnNull(world, id)) return;
        if (world.isClientSide) {
            Network.sendToServer(new RemoveEntityMessage(world.dimension(), id, true, force));
            return;
        }
        CrashUtils.runNextTick((wld) -> {
            if (force) {
                world.removeBlockEntity(TileEntityData.TEID.get(id).pos());
                world.removeBlock(TileEntityData.TEID.get(id).pos(), false);
            } else {
                world.removeBlockEntity(TileEntityData.TEID.get(id).pos());
            }
        });
    }

    public static void removeTileEntityType(Level level, ResourceLocation rl, boolean force) {
        if (NetworkTools.returnOnNull(level, rl)) return;
        if (level.isClientSide) {
            Network.sendToServer(new RemoveEntitiesMessage(level.dimension(), rl, ChunkPos.ZERO, true, force, true));
            return;
        }

        for (ChunkHolder chunk : ((ServerLevel) level).getChunkSource().chunkMap.getChunks()) {
            if (chunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING)) {
                chunk.getTickingChunk().getBlockEntities().forEach((pos, e) -> {
                    {
                        if (BlockEntityType.getKey(e.getType()).equals(rl)) {
                            CrashUtils.runNextTick((wld) -> {
                                if (force) {
                                    level.removeBlockEntity(e.getBlockPos());
                                    level.removeBlock(e.getBlockPos(), false);
                                } else {
                                    level.removeBlockEntity(e.getBlockPos());
                                }
                            });
                        }
                    }
                });
            }
        }
    }

    public static void removeTileEntitiesInChunk(Level level, ChunkPos pos, ResourceLocation rl, boolean force) {
        if (NetworkTools.returnOnNull(level, pos, rl)) return;
        if (level.isClientSide) {
            Network.sendToServer(new RemoveEntitiesMessage(level.dimension(), rl, pos, true, force,false));
            return;
        }
        Vec3 start = new Vec3(pos.getMinBlockX(), 0, pos.getMinBlockZ());
        Vec3 end = new Vec3(pos.getMaxBlockX(), 255, pos.getMaxBlockZ());

        level.blockEntityTickers.stream().filter(te -> Objects.equals(te.getType(), rl.toString()) && new AABB(start, end).contains(Vec3.atCenterOf(te.getPos()))).forEach(te -> {
            CrashUtils.runNextTick((wld) -> {
                if (force) {
                    level.removeBlockEntity(te.getPos());
                    level.removeBlock(te.getPos(), false);
                } else {
                    level.removeBlockEntity(te.getPos());
                }
            });
        });
    }
}
