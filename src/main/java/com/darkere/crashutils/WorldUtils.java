package com.darkere.crashutils;

import com.darkere.crashutils.DataStructures.TileEntityData;
import com.darkere.crashutils.Network.*;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ITeleporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public class WorldUtils {

    public static List<ServerWorld> getWorldsFromDimensionArgument(CommandContext<CommandSource> context) {
        ServerWorld world = null;
        try {
            world = DimensionArgument.getDimensionArgument(context, "dim");
        } catch (IllegalArgumentException | CommandSyntaxException e) {
            //NO OP
        }
        List<ServerWorld> worlds = new ArrayList<>();
        if (world == null) {
            context.getSource().getServer().getWorlds().forEach(worlds::add);
        } else {
            worlds.add(world);
        }
        return worlds;
    }

    public static void teleportPlayer(PlayerEntity player, World startWorld, World destWorld, BlockPos newPos) {
        if (player.world.isRemote) {
            Network.sendToServer(new TeleportMessage(startWorld.getDimensionKey(), destWorld.getDimensionKey(), newPos));
        }
        if (newPos.getY() == 0) {
            IChunk chunk = destWorld.getChunkAt(newPos);
            int y = chunk.getTopBlockY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, newPos.getX(), newPos.getZ());
            newPos = new BlockPos(newPos.getX(), y, newPos.getZ());
        }
        if (startWorld != destWorld) {
            BlockPos finalNewPos = newPos;
            player.changeDimension((ServerWorld) destWorld, new ITeleporter() {
                @Override
                public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                    Entity entity1 = repositionEntity.apply(false);
                    entity1.setPositionAndUpdate(finalNewPos.getX(), finalNewPos.getY() + 1, finalNewPos.getZ());
                    return entity1;
                }
            });
        } else {
            player.setPositionAndUpdate(newPos.getX(), newPos.getY() + 1, newPos.getZ());
        }
    }

    public static void applyToPlayer(String playerName, MinecraftServer server, Consumer<ServerPlayerEntity> consumer) {
        ServerPlayerEntity player = server.getPlayerList().getPlayerByUsername(playerName);
        if (player == null) {
            GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(playerName);
            if (profile == null) {
                return;
            }

            FakePlayer fakePlayer = new FakePlayer(server.getWorld(World.OVERWORLD), profile);
            CompoundNBT nbt = server.playerDataManager.loadPlayerData(fakePlayer);
            if (nbt == null) return;
            fakePlayer.read(nbt);
            consumer.accept(fakePlayer);
            server.playerDataManager.savePlayerData(fakePlayer);

        } else {
            consumer.accept(player);
        }
    }

    public static BlockPos getChunkCenter(ChunkPos pos) {
        int x = pos.getXStart() + 8;
        int z = pos.getZStart() + 8;
        return new BlockPos(x, 0, z);
    }

    public static void removeEntity(World world, UUID id, boolean force) {
        if(NetworkTools.returnOnNull(world,id))return;
        if (world.isRemote) {
            Network.sendToServer(new RemoveEntityMessage(world.getDimensionKey(), id, false, force));
            return;
        }
        Entity e = ((ServerWorld) world).getEntityByUuid(id);
        if (e == null) return;
        if (force) {
            ((ServerWorld) world).removeEntityComplete(e, false);
        } else {
            e.remove();
        }
    }

    public static void removeEntityType(World world, ResourceLocation rl, boolean force) {
        if(NetworkTools.returnOnNull(world,rl))return;
        if (world.isRemote) {
            Network.sendToServer(new RemoveEntitiesMessage(world.getDimensionKey(), rl, null, false, force));
            return;
        }
        ((ServerWorld) world).getEntities().filter(entity -> Objects.equals(entity.getType().getRegistryName(), rl)).forEach(e -> {
            if (force) {
                ((ServerWorld) world).removeEntityComplete(e, false);
            } else {
                e.remove();
            }
        });
    }

    public static void removeEntitiesInChunk(World world, ChunkPos pos, ResourceLocation rl, boolean force) {
        if(NetworkTools.returnOnNull(world,pos,rl))return;
        if (world.isRemote) {
            Network.sendToServer(new RemoveEntitiesMessage(world.getDimensionKey(), rl, pos, false, force));
            return;
        }
        Vector3d start = new Vector3d(pos.getXStart(), 0, pos.getZStart());
        Vector3d end = new Vector3d(pos.getXEnd(), 255, pos.getZEnd());
        world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(start, end), entity -> Objects.equals(entity.getType().getRegistryName(), rl)).forEach(e -> {
            if (force) {
                ((ServerWorld) world).removeEntityComplete(e, false);
            } else {
                e.remove();
            }
        });
    }

    public static void removeTileEntity(World world, UUID id, boolean force) {
        if(NetworkTools.returnOnNull(world,id))return;
        if (world.isRemote) {
            Network.sendToServer(new RemoveEntityMessage(world.getDimensionKey(), id, true, force));
            return;
        }
        if(force){
            world.removeTileEntity(TileEntityData.TEID.get(id).pos);
            world.removeBlock(TileEntityData.TEID.get(id).pos,false);
        } else {
            world.removeTileEntity(TileEntityData.TEID.get(id).pos);
        }
    }

    public static void removeTileEntityType(World world, ResourceLocation rl, boolean force) {
        if(NetworkTools.returnOnNull(world,rl))return;
        if (world.isRemote) {
            Network.sendToServer(new RemoveEntitiesMessage(world.getDimensionKey(), rl, null, true,force));
            return;
        }
        world.loadedTileEntityList.stream().filter(te-> Objects.equals(te.getType().getRegistryName(), rl)).forEach(te->{
            if(force){
                world.removeTileEntity(te.getPos());
                world.removeBlock(te.getPos(),false);
            } else {
                world.removeTileEntity(te.getPos());
            }
        });
    }

    public static void removeTileEntitiesInChunk(World world, ChunkPos pos, ResourceLocation rl, boolean force) {
        if(NetworkTools.returnOnNull(world,pos,rl))return;
        if (world.isRemote) {
            Network.sendToServer(new RemoveEntitiesMessage(world.getDimensionKey(), rl, pos, true,force));
            return;
        }
        Vector3d start = new Vector3d(pos.getXStart(), 0, pos.getZStart());
        Vector3d end = new Vector3d(pos.getXEnd(), 255, pos.getZEnd());

        world.loadedTileEntityList.stream().filter(te -> Objects.equals(te.getType().getRegistryName(), rl) && new AxisAlignedBB(start,end).contains(Vector3d.copyCentered(te.getPos()))).forEach(te->{
            if(force){
                world.removeTileEntity(te.getPos());
                world.removeBlock(te.getPos(),false);
            } else {
                world.removeTileEntity(te.getPos());
            }
        });
    }

}
