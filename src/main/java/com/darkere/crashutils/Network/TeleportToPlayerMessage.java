package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class TeleportToPlayerMessage {
    String name;

    public TeleportToPlayerMessage(String name) {
        this.name = name;
    }

    public static void encode(TeleportToPlayerMessage data, FriendlyByteBuf buf) {
        buf.writeUtf(data.name);
    }


    public static TeleportToPlayerMessage decode(FriendlyByteBuf buf) {
        return new TeleportToPlayerMessage(buf.readUtf(100));
    }

    public static void handle(TeleportToPlayerMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (!player.hasPermissions(CommandUtils.PERMISSION_LEVEL)) return;
            Level ori = player.getCommandSenderWorld();
            AtomicReference<Level> dest = new AtomicReference<>();
            AtomicReference<BlockPos> otherPos = new AtomicReference<>();
            WorldUtils.applyToPlayer(data.name, player.server, o -> {
                dest.set(o.getCommandSenderWorld());
                otherPos.set(o.blockPosition());
            });
            if (otherPos.get() == null) {
                player.sendMessage(new TextComponent("Failed to load Player"), new UUID(0, 0));
            }
            WorldUtils.teleportPlayer(player, ori, dest.get(), otherPos.get());
        });
        ctx.get().setPacketHandled(true);
    }
}
