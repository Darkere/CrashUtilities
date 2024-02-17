package com.darkere.crashutils.Network;

import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.DataStructures.PlayerData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.ArrayList;
import java.util.List;

public record PlayerDataMessage( PlayerData data) implements CustomPacketPayload {
    public static ResourceLocation ID = new ResourceLocation(CrashUtils.MODID,"playerdatamessage");

    public static PlayerDataMessage decode(FriendlyByteBuf buf) {
        List<String> names = new ArrayList<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            names.add(buf.readUtf());
        }
        return new PlayerDataMessage(new PlayerData(names));
    }

    public static boolean handle(PlayerDataMessage data, PlayPayloadContext ctx) {
       ctx.workHandler().submitAsync(() -> {
            DataHolder.addPlayerData(data.data);
        });
        return true;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        List<String> names = data.getPlayerNames(null);
        buf.writeInt(names.size());
        names.forEach(buf::writeUtf);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
