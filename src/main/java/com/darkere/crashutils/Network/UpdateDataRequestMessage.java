package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.DataStructures.EntityData;
import com.darkere.crashutils.DataStructures.LoadedChunkData;
import com.darkere.crashutils.DataStructures.PlayerData;
import com.darkere.crashutils.DataStructures.TileEntityData;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public record UpdateDataRequestMessage(DataRequestType RequestType, ResourceKey<Level> worldKey) implements CustomPacketPayload {

    public static final Type<UpdateDataRequestMessage> TYPE = new Type<>(CrashUtils.ResourceLocation("updatedatarequestmessage"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateDataRequestMessage> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(DataRequestType.class), UpdateDataRequestMessage::RequestType,
            ResourceKey.streamCodec(Registries.DIMENSION), UpdateDataRequestMessage::worldKey,
            UpdateDataRequestMessage::new);

    public static void handle(UpdateDataRequestMessage data, IPayloadContext ctx) {
        var player = (ServerPlayer) ctx.player();
        if (!player.hasPermissions(CommandUtils.PERMISSION_LEVEL)) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;
        ServerLevel world = server.getLevel(data.worldKey);
        List<ServerLevel> worlds = Collections.singletonList(world);
        switch (data.RequestType) {
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
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

}