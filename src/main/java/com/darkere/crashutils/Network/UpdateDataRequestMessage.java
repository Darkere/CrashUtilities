package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.DataStructures.EntityData;
import com.darkere.crashutils.DataStructures.LoadedChunkData;
import com.darkere.crashutils.DataStructures.PlayerData;
import com.darkere.crashutils.DataStructures.TileEntityData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Collections;
import java.util.List;

public record UpdateDataRequestMessage(DataRequestType type, ResourceKey<Level> worldKey) implements CustomPacketPayload {

    static ResourceLocation ID = new ResourceLocation(CrashUtils.MODID,"updatedatarequestmessage");

    public static UpdateDataRequestMessage decode(FriendlyByteBuf buf) {
        return new UpdateDataRequestMessage(
            DataRequestType.values()[buf.readInt()],
            NetworkTools.readWorldKey(buf)
        );
    }

    public static void handle(UpdateDataRequestMessage data, PlayPayloadContext ctx) {
        ctx.workHandler().submitAsync(() -> {
            if (ctx.player().isEmpty()) return;
            var player = (ServerPlayer) ctx.player().get();
            if (!player.hasPermissions(CommandUtils.PERMISSION_LEVEL)) return;
            MinecraftServer server = player.getServer();
            if (server == null) return;
            ServerLevel world = server.getLevel(data.worldKey);
            List<ServerLevel> worlds = Collections.singletonList(world);
            switch (data.type) {
                case LOADEDCHUNKDATA: {
                    LoadedChunkData loadedChunkData = new LoadedChunkData(worlds);
                    CrashUtils.runNextTick((lc) -> Network.sendToPlayer(player, new LoadedChunkDataStateMessage(loadedChunkData.getChunksByLocationType())));
                    CrashUtils.runNextTick((lc) -> Network.sendToPlayer(player, new LoadedChunkDataTicketsMessage(loadedChunkData.getChunksByTicketName())));
                    break;
                }
                case ENTITYDATA: {
                    EntityData entityData = new EntityData();
                    entityData.createLists(worlds);
                    Network.sendToPlayer(player, new EntityDataMessage(entityData));
                    break;
                }
                case TILEENTITYDATA: {
                    TileEntityData tileEntityData = new TileEntityData();
                    tileEntityData.createLists(worlds);
                    Network.sendToPlayer(player, new TileEntityDataMessage(tileEntityData));
                    break;
                }
                case PLAYERDATA: {
                    PlayerData playerData = new PlayerData();
                    playerData.createLists(worlds);
                    Network.sendToPlayer(player, new PlayerDataMessage(playerData));
                    break;
                }
            }
        });
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(type.ordinal());
        NetworkTools.writeWorldKey(worldKey, buf);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
