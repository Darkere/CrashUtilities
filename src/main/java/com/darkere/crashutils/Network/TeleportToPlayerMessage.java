package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.concurrent.atomic.AtomicReference;

public record TeleportToPlayerMessage(String name) implements CustomPacketPayload {
    public static final Type<TeleportToPlayerMessage> TYPE = new Type<>(CrashUtils.ResourceLocation("teleporttoplayermessage"));
    public static final StreamCodec<? super RegistryFriendlyByteBuf, TeleportToPlayerMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,TeleportToPlayerMessage::name,
            TeleportToPlayerMessage::new
    );

    public static TeleportToPlayerMessage decode(FriendlyByteBuf buf) {
        return new TeleportToPlayerMessage(buf.readUtf(100));
    }

    public static void handle(TeleportToPlayerMessage data, IPayloadContext ctx) {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (!player.hasPermissions(CommandUtils.PERMISSION_LEVEL)) return;
            Level ori = player.getCommandSenderWorld();
            AtomicReference<Level> dest = new AtomicReference<>();
            AtomicReference<BlockPos> otherPos = new AtomicReference<>();
            WorldUtils.applyToPlayer(data.name, player.server, o -> {
                dest.set(o.getCommandSenderWorld());
                otherPos.set(o.blockPosition());
            });
            if (otherPos.get() == null) {
                CommandUtils.sendMessageToPlayer(player,"Failed to load Player");
            }
            WorldUtils.teleportPlayer(player, ori, dest.get(), otherPos.get());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
