package com.darkere.crashutils.Network;

import com.darkere.crashutils.CrashUtils;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;

public class Network {
    private static final String NETWORK_VERSION = "2";

    @SubscribeEvent
    public static void register(RegisterPayloadHandlerEvent Event) {
        var registrar = Event.registrar(CrashUtils.MODID).versioned(NETWORK_VERSION);
        registrar.play(UpdateDataRequestMessage.ID, UpdateDataRequestMessage::decode, UpdateDataRequestMessage::handle);
        registrar.play(EntityDataMessage.ID, EntityDataMessage::decode, EntityDataMessage::handle);
        registrar.play(TeleportMessage.ID, TeleportMessage::decode, TeleportMessage::handle);
        registrar.play(PlayerInventoryRequestMessage.ID, PlayerInventoryRequestMessage::decode, PlayerInventoryRequestMessage::handle);
        registrar.play(PlayerDataMessage.ID, PlayerDataMessage::decode, PlayerDataMessage::handle);
        registrar.play(OpenPlayerInvMessage.ID, OpenPlayerInvMessage::decode, OpenPlayerInvMessage::handle);
        registrar.play(TeleportToPlayerMessage.ID, TeleportToPlayerMessage::decode, TeleportToPlayerMessage::handle);
        registrar.play(RemoveEntitiesMessage.ID, RemoveEntitiesMessage::decode, RemoveEntitiesMessage::handle);
        registrar.play(RemoveEntityMessage.ID, RemoveEntityMessage::decode, RemoveEntityMessage::handle);
        registrar.play(OpenEnderChestMessage.ID, OpenEnderChestMessage::decode, OpenEnderChestMessage::handle);
        registrar.play(LoadedChunkDataStateMessage.ID, LoadedChunkDataStateMessage::decode, LoadedChunkDataStateMessage::handle);
        registrar.play(TileEntityDataMessage.ID, TileEntityDataMessage::decode, TileEntityDataMessage::handle);
        registrar.play(LoadedChunkDataTicketsMessage.ID, LoadedChunkDataTicketsMessage::decode, LoadedChunkDataTicketsMessage::handle);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload message) {
        PacketDistributor.PLAYER.with(player).send(message);
    }

    public static void sendToServer(CustomPacketPayload message) {
        PacketDistributor.SERVER.noArg().send(message);
    }
}
