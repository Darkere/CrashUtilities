package com.darkere.crashutils.Network;

import com.darkere.crashutils.DataStructures.DataHolder;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class LoadedChunkDataStateMessage {
    Map<String, Set<ChunkPos>> loadedChunkStateData;

    LoadedChunkDataStateMessage(Map<String, Set<ChunkPos>> states) {
        loadedChunkStateData = states;
    }

    public static void encode(LoadedChunkDataStateMessage data, PacketBuffer buf) {
        if(NetworkTools.returnOnNull(data.loadedChunkStateData))return;
        NetworkTools.writeSChPMap(buf,data.loadedChunkStateData);
    }

    public static LoadedChunkDataStateMessage decode(PacketBuffer buf) {
        return new LoadedChunkDataStateMessage(NetworkTools.readSChPMap(buf));
    }

    public static void handle(LoadedChunkDataStateMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DataHolder.addStateData(data.loadedChunkStateData);
        });
        ctx.get().setPacketHandled(true);
    }


}
