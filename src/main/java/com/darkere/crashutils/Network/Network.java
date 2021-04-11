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
        INSTANCE.registerMessage(ID++, LoadedChunkDataStateMessage.class, LoadedChunkDataStateMessage::encode, LoadedChunkDataStateMessage::decode, LoadedChunkDataStateMessage::handle);
        INSTANCE.registerMessage(ID++, TeleportMessage.class, TeleportMessage::encode, TeleportMessage::decode, TeleportMessage::handle);
        INSTANCE.registerMessage(ID++, EntityDataMessage.class, EntityDataMessage::encode, EntityDataMessage::decode, EntityDataMessage::handle);
        INSTANCE.registerMessage(ID++, TileEntityDataMessage.class, TileEntityDataMessage::encode, TileEntityDataMessage::decode, TileEntityDataMessage::handle);
        INSTANCE.registerMessage(ID++, PlayerInventoryRequestMessage.class, PlayerInventoryRequestMessage::encode, PlayerInventoryRequestMessage::decode, PlayerInventoryRequestMessage::handle);
        INSTANCE.registerMessage(ID++, PlayerDataMessage.class, PlayerDataMessage::encode, PlayerDataMessage::decode, PlayerDataMessage::handle);
        INSTANCE.registerMessage(ID++, OpenPlayerInvMessage.class, OpenPlayerInvMessage::encode, OpenPlayerInvMessage::decode, OpenPlayerInvMessage::handle);
        INSTANCE.registerMessage(ID++, TeleportToPlayerMessage.class, TeleportToPlayerMessage::encode, TeleportToPlayerMessage::decode, TeleportToPlayerMessage::handle);
        INSTANCE.registerMessage(ID++, LoadedChunkDataTicketsMessage.class, LoadedChunkDataTicketsMessage::encode, LoadedChunkDataTicketsMessage::decode, LoadedChunkDataTicketsMessage::handle);
        INSTANCE.registerMessage(ID++, RemoveEntitiesMessage.class, RemoveEntitiesMessage::encode, RemoveEntitiesMessage::decode, RemoveEntitiesMessage::handle);
        INSTANCE.registerMessage(ID++, RemoveEntityMessage.class, RemoveEntityMessage::encode, RemoveEntityMessage::decode, RemoveEntityMessage::handle);
        INSTANCE.registerMessage(ID++, SplitPacketMessage.class, SplitPacketMessage::encode, SplitPacketMessage::decode,SplitPacketMessage::handle);

    }

    public static void sendToPlayer(ServerPlayerEntity playerEntity, Object Message) {
        if (!(playerEntity instanceof FakePlayer)) {
            Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> playerEntity), Message);
        }
    }

    public static void sendToServer(Object Message) {
        Network.INSTANCE.sendToServer(Message);
    }

//    /**
//     * Method that handles the splitting of the message into chunks if need be.
//     *
//     * @param msg                  The message to split in question.
//     * @param splitMessageConsumer The consumer that sends away the split parts of the message.
//     */
//    private void handleSplitting(final IMessage msg, final Consumer<IMessage> splitMessageConsumer)
//
//
//        //Write the message into a buffer and copy that buffer into a byte array for processing.
//        final ByteBuf buffer = Unpooled.buffer();
//        final PacketBuffer innerPacketBuffer = new PacketBuffer(buffer);
//        msg.toBytes(innerPacketBuffer);
//        final byte[] data = buffer.array();
//        buffer.release();
//
//        //Some tracking variables.
//        //Max packet size: 90% of maximum.
//        final int max_packet_size = 943718; //This is 90% of max packet size.
//        //The current index in the data array.
//        int currentIndex = 0;
//        //The current index for the split packets.
//        int packetIndex = 0;
//        //The communication id.
//        final int comId = messageCounter.getAndIncrement();
//
//        //Loop while data is available.
//        while (currentIndex < data.length)
//        {
//            //Tell the network message entry that we are splitting a packet.
//            this.getMessagesTypes().get(messageId).onSplitting(packetIndex);
//
//            final int extra = Math.min(max_packet_size, data.length - currentIndex);
//            //Extract the sub data array.
//            final byte[] subPacketData = Arrays.copyOfRange(data, currentIndex, currentIndex + extra);
//
//            //Construct the wrapping packet.
//            final SplitPacketMessage splitPacketMessage = new SplitPacketMessage(comId, packetIndex++, (currentIndex + extra) >= data.length, messageId, subPacketData);
//
//            //Send the wrapping packet.
//            splitMessageConsumer.accept(splitPacketMessage);
//
//            //Move our working index.
//            currentIndex += extra;
//        }
//    }
}
