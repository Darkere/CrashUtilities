package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.WorldUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class RemoveEntitiesMessage {
    ResourceKey<Level> worldRegistryKey;
    ResourceLocation rl;
    ChunkPos pos;
    boolean tile;
    boolean force;

    public RemoveEntitiesMessage(ResourceKey<Level> worldRegistryKey, ResourceLocation rl, ChunkPos pos, boolean tile, boolean force) {
        this.worldRegistryKey = worldRegistryKey;
        this.rl = rl;
        this.pos = pos;
        this.tile = tile;
        this.force = force;
    }


    public static void encode(RemoveEntitiesMessage data, FriendlyByteBuf buf) {
        NetworkTools.writeWorldKey(data.worldRegistryKey, buf);
        buf.writeResourceLocation(data.rl);
        buf.writeBoolean(data.tile);
        buf.writeBoolean(data.force);
        if (data.pos == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeLong(data.pos.toLong());
        }
    }


    public static RemoveEntitiesMessage decode(FriendlyByteBuf buf) {
        ResourceKey<Level> world = NetworkTools.readWorldKey(buf);
        ResourceLocation rl = buf.readResourceLocation();
        boolean tile = buf.readBoolean();
        boolean force = buf.readBoolean();
        ChunkPos pos = null;
        if (buf.readBoolean()) {
            pos = new ChunkPos(buf.readLong());
        }
        return new RemoveEntitiesMessage(world, rl, pos, tile, force);
    }

    public static boolean handle(RemoveEntitiesMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.hasPermissions(CommandUtils.PERMISSION_LEVEL)) return;
            Level world = player.getServer().getLevel(data.worldRegistryKey);
            if (data.pos == null) {
                if (data.tile) {
                    WorldUtils.removeTileEntityType(world, data.rl, data.force);
                } else {
                    WorldUtils.removeEntityType(world, data.rl, data.force);
                }
            } else {
                if (data.tile) {
                    WorldUtils.removeTileEntitiesInChunk(world, data.pos, data.rl, data.force);
                } else {
                    WorldUtils.removeEntitiesInChunk(world, data.pos, data.rl, data.force);
                }
            }
        });
        return true;
    }
}
