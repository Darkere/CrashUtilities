package com.darkere.crashutils.Network;

import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.DataStructures.PlayerData;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PlayerDataMessage {
    private PlayerData data;
    public PlayerDataMessage(PlayerData playerData) {
        data = playerData;
    }
    public static void encode(PlayerDataMessage data, PacketBuffer buf) {
        List<String> names = data.data.getPlayerNames(null);
        buf.writeInt(names.size());
        names.forEach(buf::writeString);
    }


    public static PlayerDataMessage decode(PacketBuffer buf) {
        List<String> names = new ArrayList<>();
        int size = buf.readInt();
        for (int i = 0; i<size;i++){
            names.add(buf.readString());
        }
        return new PlayerDataMessage(new PlayerData(names));
    }

    public static boolean handle(PlayerDataMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DataHolder.addPlayerData(data.data);
        });
        return true;
    }
}
