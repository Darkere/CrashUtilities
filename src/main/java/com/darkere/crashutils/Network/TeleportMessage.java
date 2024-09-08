package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.WorldUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record TeleportMessage( ResourceKey<Level> origin,  ResourceKey<Level> dest, BlockPos pos) implements CustomPacketPayload {

    public static final Type<TeleportMessage> TYPE = new Type<>(CrashUtils.ResourceLocation("teleportmessage" ));
    public static final StreamCodec<? super RegistryFriendlyByteBuf, TeleportMessage> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), TeleportMessage::origin,
            ResourceKey.streamCodec(Registries.DIMENSION), TeleportMessage::dest,
            BlockPos.STREAM_CODEC,TeleportMessage::pos,
            TeleportMessage::new
    );

    public static void handle(TeleportMessage data, IPayloadContext ctx) {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if(!player.hasPermissions(CommandUtils.PERMISSION_LEVEL)) return;
            ServerLevel ori = player.getServer().getLevel(data.origin);
            ServerLevel dest = player.getServer().getLevel(data.dest);
            WorldUtils.teleportPlayer(player, ori, dest, data.pos);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
