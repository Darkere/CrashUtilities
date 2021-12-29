package com.darkere.crashutils.Network;

import com.darkere.crashutils.DataStructures.DataHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class LoadedChunkDataTicketsMessage {
    Map<String, Set<ChunkPos>> data;

    LoadedChunkDataTicketsMessage(Map<String, Set<ChunkPos>> data) {
        this.data = data;
    }

    public static void encode(LoadedChunkDataTicketsMessage data, FriendlyByteBuf buf) {
        NetworkTools.writeSChPMap(buf, data.data);
    }

    public static LoadedChunkDataTicketsMessage decode(FriendlyByteBuf buf) {
        return new LoadedChunkDataTicketsMessage(NetworkTools.readSChPMap(buf));
    }

    public static void handle(LoadedChunkDataTicketsMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DataHolder.addTicketData(data.data);
        });
        ctx.get().setPacketHandled(true);
    }
}
