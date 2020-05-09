package com.darkere.crashutils.Network;

import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.DataStructures.LoadedChunkData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class LoadedChunkDataMessage {
    LoadedChunkData loadedChunkData;

    LoadedChunkDataMessage(LoadedChunkData chunkList) {
        loadedChunkData = chunkList;
    }

    public static void encode(LoadedChunkDataMessage data, PacketBuffer buf) {
        NetworkTools.writeSChPMap(buf, data.loadedChunkData.getChunksByTicketName());
        NetworkTools.writeSChPMap(buf, data.loadedChunkData.getChunksByLocationType());
    }

    public static LoadedChunkDataMessage decode(PacketBuffer buf) {
        return new LoadedChunkDataMessage(new LoadedChunkData(
            NetworkTools.readSChPMap(buf),
            NetworkTools.readSChPMap(buf)
        ));
    }

    public static void handle(LoadedChunkDataMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DataHolder.addLoadedChunkData(data.loadedChunkData);
        });
        ctx.get().setPacketHandled(true);
    }


}
