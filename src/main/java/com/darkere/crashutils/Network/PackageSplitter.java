package com.darkere.crashutils.Network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PackageSplitter {
    private static final int max_packet_size = 1000;//943718;
    private static int ID = 0;
    public static final Map<Integer, Map<Integer, byte[]>> packageCache = new HashMap<>();
    public static final Map<Integer, PacketBuffer> fullPackageCache = new HashMap<>();

    public static int potentiallySplitPacket(ServerPlayerEntity player, PacketBuffer buffer) {

        byte[] data = buffer.array();
        if (data.length < max_packet_size) {
            return -1;
        }
        int currentIndex = 0;
        //The current index for the split packets.
        int packetIndex = 0;
        //The communication id.
        int comId = ID++;

        //Loop while data is available.
        while (currentIndex < data.length) {

            int extra = Math.min(max_packet_size, data.length - currentIndex);
            //Extract the sub data array.
            byte[] subPacketData = Arrays.copyOfRange(data, currentIndex, currentIndex + extra);

            //Construct the wrapping packet.
            SplitPacketMessage splitPacketMessage = new SplitPacketMessage(comId, packetIndex++, (currentIndex + extra) >= data.length, subPacketData);
            //Send the wrapping packet.
            Network.sendToPlayer(player, splitPacketMessage);

            //Move our working index.
            currentIndex += extra;
        }

        return comId;
    }

    public static void onFinishedPackageSplit(int id) {
        packageCache.get(id).clear();
        packageCache.remove(id);
        fullPackageCache.get(id).release();
        fullPackageCache.remove(id);
    }
}
