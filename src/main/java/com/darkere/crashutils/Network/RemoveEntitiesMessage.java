package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.WorldUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record RemoveEntitiesMessage(ResourceKey<Level> worldRegistryKey,ResourceLocation rl, ChunkPos pos,boolean tile,boolean force) implements CustomPacketPayload {
    public static ResourceLocation ID = new ResourceLocation(CrashUtils.MODID,"removeentitiesmessage");
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

    public static boolean handle(RemoveEntitiesMessage data, PlayPayloadContext ctx) {
       ctx.workHandler().submitAsync(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player().get();
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

    @Override
    public void write(FriendlyByteBuf buf) {
        NetworkTools.writeWorldKey(worldRegistryKey, buf);
        buf.writeResourceLocation(rl);
        buf.writeBoolean(tile);
        buf.writeBoolean(force);
        if (pos == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeLong(pos.toLong());
        }
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
