package com.darkere.crashutils.Network;

import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.DataStructures.PlayerData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record PlayerDataMessage( PlayerData data) implements CustomPacketPayload {
    public static final Type<PlayerDataMessage> TYPE = new Type<>(CrashUtils.ResourceLocation("playerdatamessage"));
    public static final StreamCodec<? super RegistryFriendlyByteBuf, PlayerDataMessage> STREAM_CODEC = StreamCodec.composite(
            PlayerData.STREAM_CODEC,PlayerDataMessage::data,
            PlayerDataMessage::new
    );


    public static boolean handle(PlayerDataMessage data, IPayloadContext ctx) {
            DataHolder.addPlayerData(data.data);
        return true;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
