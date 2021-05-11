package com.darkere.crashutils.Network;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Represents a class that wrappers other messages in byte form and is used to split the wrapped messages data into several chunks.
 */
public class SplitPacketMessage {
    /**
     * Internal communication id. Used to indicate to what wrapped message this belongs to.
     */
    private int communicationId;

    /**
     * The index of the split message in the wrapped message.
     */
    private int packetIndex;

    /**
     * The payload.
     */
    private final byte[] payload;

    public SplitPacketMessage(final int communicationId, final int packetIndex, final byte[] payload) {
        this.communicationId = communicationId;
        this.packetIndex = packetIndex;
        this.payload = payload;
    }

    public static void encode(SplitPacketMessage message, PacketBuffer buf) {
        buf.writeVarInt(message.communicationId);
        buf.writeVarInt(message.packetIndex);
        buf.writeByteArray(message.payload);
    }

    public static SplitPacketMessage decode(final PacketBuffer buf) {
        return new SplitPacketMessage(buf.readVarInt(), buf.readVarInt(), buf.readByteArray());
    }

    public static boolean handle(SplitPacketMessage data, Supplier<NetworkEvent.Context> ctx) {
        Network.addPackagePart(data.communicationId,data.packetIndex,data.payload);
        ctx.get().setPacketHandled(true);
        return true;
    }
}
