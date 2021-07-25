package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.DataStructures.EntityData;
import com.darkere.crashutils.DataStructures.LoadedChunkData;
import com.darkere.crashutils.DataStructures.PlayerData;
import com.darkere.crashutils.DataStructures.TileEntityData;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class UpdateDataRequestMessage {
    private DataRequestType type;
    private RegistryKey<World> worldKey;

    public UpdateDataRequestMessage(DataRequestType type, RegistryKey<World> worldKey) {
        this.type = type;
        this.worldKey = worldKey;
    }


    public static void encode(UpdateDataRequestMessage data, PacketBuffer buf) {
        buf.writeInt(data.type.ordinal());
        NetworkTools.writeWorldKey(data.worldKey, buf);
    }


    public static UpdateDataRequestMessage decode(PacketBuffer buf) {
        return new UpdateDataRequestMessage(
            DataRequestType.values()[buf.readInt()],
            NetworkTools.readWorldKey(buf)
        );
    }

    public static void handle(UpdateDataRequestMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ctx.get().getSender() == null) return;
            if (!ctx.get().getSender().hasPermissions(CommandUtils.PERMISSION_LEVEL)) return;
            MinecraftServer server = ctx.get().getSender().getServer();
            if (server == null) return;
            ServerWorld world = server.getLevel(data.worldKey);
            List<ServerWorld> worlds = Collections.singletonList(world);
            switch (data.type) {
                case LOADEDCHUNKDATA: {
                    LoadedChunkData loadedChunkData = new LoadedChunkData(worlds);
                    CrashUtils.runNextTick((lc) -> Network.sendToPlayer(ctx.get().getSender(), new LoadedChunkDataStateMessage(loadedChunkData.getChunksByLocationType())));
                    CrashUtils.runNextTick((lc) -> Network.sendToPlayer(ctx.get().getSender(), new LoadedChunkDataTicketsMessage(loadedChunkData.getChunksByTicketName())));
                    break;
                }
                case ENTITYDATA: {
                    EntityData entityData = new EntityData();
                    entityData.createLists(worlds);
                    Network.sendToPlayer(ctx.get().getSender(), new EntityDataMessage(entityData));
                    break;
                }
                case TILEENTITYDATA: {
                    TileEntityData tileEntityData = new TileEntityData();
                    tileEntityData.createLists(worlds);
                    Network.sendToPlayer(ctx.get().getSender(), new TileEntityDataMessage(tileEntityData));
                    break;
                }
                case PLAYERDATA: {
                    PlayerData playerData = new PlayerData();
                    playerData.createLists(worlds);
                    Network.sendToPlayer(ctx.get().getSender(), new PlayerDataMessage(playerData));
                    break;
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
