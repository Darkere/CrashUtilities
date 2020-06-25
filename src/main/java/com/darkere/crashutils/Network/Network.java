package com.darkere.crashutils.Network;

import com.darkere.crashutils.CrashUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class Network {
    public static SimpleChannel INSTANCE;
    private static int ID = 0;
    private static final ResourceLocation CHANNELID = new ResourceLocation(CrashUtils.MODID + ":" + "network");

    public static void register() {
        INSTANCE = NetworkRegistry.newSimpleChannel(CHANNELID, () -> "1", s -> true, s -> true);

        INSTANCE.registerMessage(ID++, UpdateDataRequestMessage.class, UpdateDataRequestMessage::encode, UpdateDataRequestMessage::decode, UpdateDataRequestMessage::handle);
        INSTANCE.registerMessage(ID++, LoadedChunkDataMessage.class, LoadedChunkDataMessage::encode, LoadedChunkDataMessage::decode, LoadedChunkDataMessage::handle);
        INSTANCE.registerMessage(ID++,TeleportMessage.class,TeleportMessage::encode,TeleportMessage::decode,TeleportMessage::handle);
        INSTANCE.registerMessage(ID++, EntityDataMessage.class, EntityDataMessage::encode, EntityDataMessage::decode, EntityDataMessage::handle);
        INSTANCE.registerMessage(ID++, TileEntityDataMessage.class, TileEntityDataMessage::encode, TileEntityDataMessage::decode, TileEntityDataMessage::handle);
        INSTANCE.registerMessage(ID++, PlayerInventoryRequestMessage.class, PlayerInventoryRequestMessage::encode, PlayerInventoryRequestMessage::decode, PlayerInventoryRequestMessage::handle);
        INSTANCE.registerMessage(ID++, PlayerDataMessage.class, PlayerDataMessage::encode, PlayerDataMessage::decode, PlayerDataMessage::handle);
        INSTANCE.registerMessage(ID++, OpenPlayerInvMessage.class, OpenPlayerInvMessage::encode, OpenPlayerInvMessage::decode, OpenPlayerInvMessage::handle);

    }

    public static void sendToPlayer(ServerPlayerEntity playerEntity, Object Message){
        if(!(playerEntity instanceof FakePlayer)){
            Network.INSTANCE.send(PacketDistributor.PLAYER.with(()->  playerEntity),Message);
        }
    }
    public static void sendToServer(Object Message){
        Network.INSTANCE.sendToServer(Message);
    }
}
