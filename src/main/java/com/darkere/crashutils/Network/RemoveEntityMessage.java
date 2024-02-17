package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.WorldUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.UUID;

public record RemoveEntityMessage(ResourceKey<Level> worldRegistryKey, UUID uuid, boolean tile,boolean force ) implements CustomPacketPayload {
    public static ResourceLocation ID = new ResourceLocation(CrashUtils.MODID,"removeentitymessage");


    public static RemoveEntityMessage decode(FriendlyByteBuf buf) {
        return new RemoveEntityMessage(NetworkTools.readWorldKey(buf), buf.readUUID(), buf.readBoolean(), buf.readBoolean());
    }

    public static boolean handle(RemoveEntityMessage data, PlayPayloadContext ctx) {
       ctx.workHandler().submitAsync(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player().get();
            if (!player.hasPermissions(CommandUtils.PERMISSION_LEVEL)) return;
            if (data.tile) {
                WorldUtils.removeTileEntity(player.getServer().getLevel(data.worldRegistryKey), data.uuid, data.force);
            } else {
                WorldUtils.removeEntity(player.getServer().getLevel(data.worldRegistryKey), data.uuid);
            }
        });
        return true;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        NetworkTools.writeWorldKey(worldRegistryKey, buf);
        buf.writeUUID(uuid);
        buf.writeBoolean(tile);
        buf.writeBoolean(force);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
