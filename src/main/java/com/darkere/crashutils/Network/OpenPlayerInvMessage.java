package com.darkere.crashutils.Network;

import com.darkere.crashutils.ClientEvents;
import com.darkere.crashutils.CrashUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.LinkedHashMap;
import java.util.Map;

public record OpenPlayerInvMessage(Map<String, Integer> slotAmounts, String otherPlayerName,int windowID) implements CustomPacketPayload {


    public static final Type<OpenPlayerInvMessage> TYPE = new Type<>(CrashUtils.ResourceLocation("openplayerinvmessage"));
    public static final StreamCodec<? super RegistryFriendlyByteBuf, OpenPlayerInvMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(LinkedHashMap::new,ByteBufCodecs.STRING_UTF8,ByteBufCodecs.INT),OpenPlayerInvMessage::slotAmounts,
            ByteBufCodecs.STRING_UTF8,OpenPlayerInvMessage::otherPlayerName,
            ByteBufCodecs.INT,OpenPlayerInvMessage::windowID,
            OpenPlayerInvMessage::new
    );

    public static boolean handle(OpenPlayerInvMessage data, IPayloadContext ctx) {
        ClientEvents.openContainerAndScreen(data.windowID, data.otherPlayerName, data.slotAmounts);
        return true;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
