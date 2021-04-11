package com.darkere.crashutils.Network;

import com.google.common.primitives.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Represents a class that wrappers other messages in byte form and is used to split the wrapped messages data into several chunks.
 */
public class SplitPacketMessage {
    /**
     * Internal communication id. Used to indicate to what wrapped message this belongs to.
     */
    private int communicationId = -1;

    /**
     * The index of the split message in the wrapped message.
     */
    private int packetIndex = -1;

    /**
     * Indicates if this is the last message in the chain.
     */
    private boolean terminator = false;

    /**
     * The payload.
     */
    private byte[] payload;

    /**
     * The network receiving constructor.
     */
    public SplitPacketMessage() {
    }

    public SplitPacketMessage(final int communicationId, final int packetIndex, final boolean terminator, final byte[] payload) {
        this.communicationId = communicationId;
        this.packetIndex = packetIndex;
        this.terminator = terminator;
        this.payload = payload;
    }

    public static void encode(SplitPacketMessage message, PacketBuffer buf) {
        buf.writeVarInt(message.communicationId);
        buf.writeVarInt(message.packetIndex);
        buf.writeBoolean(message.terminator);
        buf.writeByteArray(message.payload);
    }

    public static SplitPacketMessage decode(final PacketBuffer buf) {
      return new SplitPacketMessage(buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readByteArray());
    }

    public static boolean handle(SplitPacketMessage data, Supplier<NetworkEvent.Context> ctx) {

        //Sync on the message cache since this is still on the Netty thread.
        synchronized (PackageSplitter.packageCache) {
            PackageSplitter.packageCache.computeIfAbsent(data.communicationId, (id) -> new ConcurrentHashMap<>());
            PackageSplitter.packageCache.get(data.communicationId).put(data.packetIndex, data.payload);
        }

        if (!data.terminator) {
            //We are not the last message, stop executing.
            ctx.get().setPacketHandled(true);
            return true;
        }

        //No need to sync again, since we are now the last packet to arrive.
        //All data gets sorted and appended.
        final byte[] packetData = PackageSplitter.packageCache.get(data.communicationId).entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .reduce(new byte[0], Bytes::concat);

        final ByteBuf buffer = Unpooled.wrappedBuffer(packetData);
        PackageSplitter.fullPackageCache.put(data.communicationId, new PacketBuffer(buffer));
        ctx.get().setPacketHandled(true);
        return true;
    }
}
