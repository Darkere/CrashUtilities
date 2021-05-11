package com.darkere.crashutils.Network;

import com.darkere.crashutils.CrashUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Network {
    private static int ID = 1;

    private static final String NETWORK_VERSION = "2";
    private static final ResourceLocation CHANNEL_ID = new ResourceLocation(CrashUtils.MODID + ":" + "network");
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(CHANNEL_ID, () -> NETWORK_VERSION, s -> s.equals(NETWORK_VERSION), s -> s.equals(NETWORK_VERSION));
    private static final PacketSplitter SPLITTER = new PacketSplitter(10, CHANNEL, CHANNEL_ID);

    public static void register() {

        CHANNEL.registerMessage(ID++, UpdateDataRequestMessage.class, UpdateDataRequestMessage::encode, UpdateDataRequestMessage::decode, UpdateDataRequestMessage::handle);
        CHANNEL.registerMessage(ID++, TeleportMessage.class, TeleportMessage::encode, TeleportMessage::decode, TeleportMessage::handle);
        CHANNEL.registerMessage(ID++, PlayerInventoryRequestMessage.class, PlayerInventoryRequestMessage::encode, PlayerInventoryRequestMessage::decode, PlayerInventoryRequestMessage::handle);
        CHANNEL.registerMessage(ID++, PlayerDataMessage.class, PlayerDataMessage::encode, PlayerDataMessage::decode, PlayerDataMessage::handle);
        CHANNEL.registerMessage(ID++, OpenPlayerInvMessage.class, OpenPlayerInvMessage::encode, OpenPlayerInvMessage::decode, OpenPlayerInvMessage::handle);
        CHANNEL.registerMessage(ID++, TeleportToPlayerMessage.class, TeleportToPlayerMessage::encode, TeleportToPlayerMessage::decode, TeleportToPlayerMessage::handle);
        CHANNEL.registerMessage(ID++, RemoveEntitiesMessage.class, RemoveEntitiesMessage::encode, RemoveEntitiesMessage::decode, RemoveEntitiesMessage::handle);
        CHANNEL.registerMessage(ID++, RemoveEntityMessage.class, RemoveEntityMessage::encode, RemoveEntityMessage::decode, RemoveEntityMessage::handle);

        CHANNEL.registerMessage(ID++, SplitPacketMessage.class, SplitPacketMessage::encode, SplitPacketMessage::decode, SplitPacketMessage::handle);

        SPLITTER.registerMessage(ID++, LoadedChunkDataStateMessage.class, LoadedChunkDataStateMessage::encode, LoadedChunkDataStateMessage::decode, LoadedChunkDataStateMessage::handle);
        SPLITTER.registerMessage(ID++, EntityDataMessage.class, EntityDataMessage::encode, EntityDataMessage::decode, EntityDataMessage::handle);
        SPLITTER.registerMessage(ID++, TileEntityDataMessage.class, TileEntityDataMessage::encode, TileEntityDataMessage::decode, TileEntityDataMessage::handle);
        SPLITTER.registerMessage(ID++, LoadedChunkDataTicketsMessage.class, LoadedChunkDataTicketsMessage::encode, LoadedChunkDataTicketsMessage::decode, LoadedChunkDataTicketsMessage::handle);
    }

    public static void sendToPlayer(ServerPlayerEntity player, Object message) {
        if (!(player instanceof FakePlayer)) {
            if (SPLITTER.shouldMessageBeSplit(message.getClass())) {
                SPLITTER.sendToPlayer(player, message);
            } else {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
            }
        }
    }

    public static void sendToServer(Object message) {
        if (SPLITTER.shouldMessageBeSplit(message.getClass())) {
            SPLITTER.sendToServer(message);
        } else {
            CHANNEL.send(PacketDistributor.SERVER.noArg(), message);
        }
    }


    public static void addPackagePart(int communicationId, int packetIndex, byte[] payload) {
        SPLITTER.addPackagePart(communicationId, packetIndex, payload);
    }
}
