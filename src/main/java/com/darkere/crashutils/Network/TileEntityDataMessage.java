package com.darkere.crashutils.Network;

import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.DataStructures.TileEntityData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TileEntityDataMessage {
    TileEntityData list;

    public TileEntityDataMessage(TileEntityData list) {
        this.list = list;
    }


    public static void encode(TileEntityDataMessage data, PacketBuffer buf) {
        NetworkTools.writeRLWPMap(data.list.getMap(), buf);
    }


    public static TileEntityDataMessage decode(PacketBuffer buf) {
        return new TileEntityDataMessage(new TileEntityData(NetworkTools.readRLWPMap(buf)));
    }

    public static void handle(TileEntityDataMessage data, Supplier<NetworkEvent.Context> ctx) {

        ctx.get().enqueueWork(() -> DataHolder.addTileEntityData(data.list));
        ctx.get().setPacketHandled(true);
    }
}
