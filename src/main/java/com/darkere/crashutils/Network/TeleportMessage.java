package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TeleportMessage {
    ResourceKey<Level> origin;
    ResourceKey<Level> dest;
    BlockPos pos;

    public TeleportMessage(ResourceKey<Level> origin, ResourceKey<Level> dest, BlockPos pos) {
        this.origin = origin;
        this.dest = dest;
        this.pos = pos;
    }

    public static void encode(TeleportMessage data, FriendlyByteBuf buf) {
        NetworkTools.writeWorldKey(data.origin, buf);
        NetworkTools.writeWorldKey(data.dest, buf);
        buf.writeBlockPos(data.pos);
    }


    public static TeleportMessage decode(FriendlyByteBuf buf) {
        return new TeleportMessage(
            NetworkTools.readWorldKey(buf),
            NetworkTools.readWorldKey(buf),
            buf.readBlockPos()
        );
    }

    public static void handle(TeleportMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if(!player.hasPermissions(CommandUtils.PERMISSION_LEVEL)) return;
            ServerLevel ori = player.getServer().getLevel(data.origin);
            ServerLevel dest = player.getServer().getLevel(data.dest);
            WorldUtils.teleportPlayer(player, ori, dest, data.pos);
        });
        ctx.get().setPacketHandled(true);
    }
}
