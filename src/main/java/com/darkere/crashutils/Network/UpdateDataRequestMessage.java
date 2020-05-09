package com.darkere.crashutils.Network;

import com.darkere.crashutils.DataStructures.EntityData;
import com.darkere.crashutils.DataStructures.LoadedChunkData;
import com.darkere.crashutils.DataStructures.TileEntityData;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class UpdateDataRequestMessage {
    private DataRequestType type;
    private DimensionType dim;

    public UpdateDataRequestMessage(DataRequestType type, DimensionType dim) {
        this.type = type;
        this.dim = dim;
    }


    public static void encode(UpdateDataRequestMessage data, PacketBuffer buf) {
        buf.writeInt(data.type.ordinal());
        NetworkTools.writeDimensionType(data.dim,buf);
    }


    public static UpdateDataRequestMessage decode(PacketBuffer buf) {
        return new UpdateDataRequestMessage(
            DataRequestType.values()[buf.readInt()],
            NetworkTools.readDimensionType(buf)
        );
    }

    public static void handle(UpdateDataRequestMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if(ctx.get().getSender() == null)return;
            if(!ctx.get().getSender().hasPermissionLevel(4)) return;
            MinecraftServer server = ctx.get().getSender().getServer();
            if (server == null) return;
            ServerWorld world = DimensionManager.getWorld(server, data.dim, false, false);
            List<ServerWorld> worlds = Collections.singletonList(world);
            switch (data.type) {
                case LOADEDCHUNKDATA: {
                    LoadedChunkData loadedChunkData = new LoadedChunkData(worlds);
                    Network.sendToPlayer(ctx.get().getSender(), new LoadedChunkDataMessage(loadedChunkData));
                    break;
                }
                case ENTITYDATA: {
                    EntityData entityData = new EntityData();
                    entityData.createLists(worlds);
                    Network.sendToPlayer(ctx.get().getSender(),new EntityDataMessage(entityData));
                    break;
                }
                case TILEENTITYDATA: {
                    TileEntityData tileEntityData = new TileEntityData();
                    tileEntityData.createLists(worlds);
                    Network.sendToPlayer(ctx.get().getSender(),new TileEntityDataMessage(tileEntityData));
                    break;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
