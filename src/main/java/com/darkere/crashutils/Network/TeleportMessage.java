package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record TeleportMessage( ResourceKey<Level> origin,  ResourceKey<Level> dest, BlockPos pos) implements CustomPacketPayload {

    public static ResourceLocation ID = new ResourceLocation(CrashUtils.MODID,"teleportmessage" );

    public static TeleportMessage decode(FriendlyByteBuf buf) {
        return new TeleportMessage(
            NetworkTools.readWorldKey(buf),
            NetworkTools.readWorldKey(buf),
            buf.readBlockPos()
        );
    }

    public static void handle(TeleportMessage data, PlayPayloadContext ctx) {
       ctx.workHandler().submitAsync(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player().get();
            if(!player.hasPermissions(CommandUtils.PERMISSION_LEVEL)) return;
            ServerLevel ori = player.getServer().getLevel(data.origin);
            ServerLevel dest = player.getServer().getLevel(data.dest);
            WorldUtils.teleportPlayer(player, ori, dest, data.pos);
        });
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        NetworkTools.writeWorldKey(origin, buf);
        NetworkTools.writeWorldKey(dest, buf);
        buf.writeBlockPos(pos);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
