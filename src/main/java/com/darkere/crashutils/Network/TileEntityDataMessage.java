package com.darkere.crashutils.Network;

import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.DataStructures.TileEntityData;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TileEntityDataMessage {
    TileEntityData list;
    PacketBuffer packetBuffer;
    ServerPlayerEntity player;

    public TileEntityDataMessage(ServerPlayerEntity player, TileEntityData list) {
        this.player = player;
        this.list = list;
    }

    public TileEntityDataMessage(PacketBuffer buf) {
        this.packetBuffer = buf;
    }


    public static void encode(TileEntityDataMessage data, PacketBuffer buf) {
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        NetworkTools.writeRLWPMap(data.list.getMap(), buffer);
        int id = PackageSplitter.potentiallySplitPacket(data.player, buffer);
        buf.writeInt(id);
        if (id == -1) {
            return;
        }
        buf.writeByteArray(buf.array());
    }


    public static TileEntityDataMessage decode(PacketBuffer buf) {
        return new TileEntityDataMessage(buf);
    }

    public static void handle(TileEntityDataMessage data, Supplier<NetworkEvent.Context> ctx) {
        int id = data.packetBuffer.readInt();
        if (id == -1) {
            data.list = new TileEntityData(NetworkTools.readRLWPMap(data.packetBuffer));
        } else {
            data.list = new TileEntityData(NetworkTools.readRLWPMap(PackageSplitter.fullPackageCache.get(id)));
            PackageSplitter.onFinishedPackageSplit(id);
        }

        ctx.get().enqueueWork(() -> {
            DataHolder.addTileEntityData(data.list);

        });
        ctx.get().setPacketHandled(true);
    }
}
