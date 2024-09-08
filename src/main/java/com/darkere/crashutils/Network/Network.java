package com.darkere.crashutils.Network;

import com.darkere.crashutils.CrashUtils;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class Network {
    private static final String NETWORK_VERSION = "2";

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent Event) {
        var registrar = Event.registrar(CrashUtils.MODID).versioned(NETWORK_VERSION).optional();
        registrar.playBidirectional(UpdateDataRequestMessage.TYPE, UpdateDataRequestMessage.STREAM_CODEC, UpdateDataRequestMessage::handle);
        registrar.playBidirectional(EntityDataMessage.TYPE, EntityDataMessage.STREAM_CODEC, EntityDataMessage::handle);
        registrar.playBidirectional(TeleportMessage.TYPE, TeleportMessage.STREAM_CODEC, TeleportMessage::handle);
        registrar.playBidirectional(PlayerInventoryRequestMessage.TYPE, PlayerInventoryRequestMessage.STREAM_CODEC, PlayerInventoryRequestMessage::handle);
        registrar.playBidirectional(PlayerDataMessage.TYPE, PlayerDataMessage.STREAM_CODEC, PlayerDataMessage::handle);
        registrar.playBidirectional(OpenPlayerInvMessage.TYPE, OpenPlayerInvMessage.STREAM_CODEC, OpenPlayerInvMessage::handle);
        registrar.playBidirectional(TeleportToPlayerMessage.TYPE, TeleportToPlayerMessage.STREAM_CODEC, TeleportToPlayerMessage::handle);
        registrar.playBidirectional(RemoveEntitiesMessage.TYPE, RemoveEntitiesMessage.STREAM_CODEC, RemoveEntitiesMessage::handle);
        registrar.playBidirectional(RemoveEntityMessage.TYPE, RemoveEntityMessage.STREAM_CODEC, RemoveEntityMessage::handle);
        registrar.playBidirectional(OpenEnderChestMessage.TYPE, OpenEnderChestMessage.STREAM_CODEC, OpenEnderChestMessage::handle);
        registrar.playBidirectional(LoadedChunkDataStateMessage.TYPE, LoadedChunkDataStateMessage.STREAM_CODEC, LoadedChunkDataStateMessage::handle);
        registrar.playBidirectional(TileEntityDataMessage.TYPE, TileEntityDataMessage.STREAM_CODEC, TileEntityDataMessage::handle);
        registrar.playBidirectional(LoadedChunkDataTicketsMessage.TYPE, LoadedChunkDataTicketsMessage.STREAM_CODEC, LoadedChunkDataTicketsMessage::handle);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload message) {
        PacketDistributor.sendToPlayer(player,message);
    }

    public static void sendToServer(CustomPacketPayload message) {
        PacketDistributor.sendToServer(message);
    }
}
