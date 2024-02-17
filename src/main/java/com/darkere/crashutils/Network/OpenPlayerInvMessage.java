package com.darkere.crashutils.Network;

import com.darkere.crashutils.ClientEvents;
import com.darkere.crashutils.CrashUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.LinkedHashMap;
import java.util.Map;

public record OpenPlayerInvMessage(Map<String, Integer> slotAmounts, String otherPlayerName,int windowID) implements CustomPacketPayload {


    public static ResourceLocation ID = new ResourceLocation(CrashUtils.MODID,"openplayerinvmessage");

    public static void encode(OpenPlayerInvMessage data, FriendlyByteBuf buf) {


    }

    public static OpenPlayerInvMessage decode(FriendlyByteBuf buf) {
        Map<String, Integer> curios = new LinkedHashMap<>();
        int id = buf.readInt();
        String name = buf.readUtf();
        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            curios.put(buf.readUtf(), buf.readInt());
        }
        return new OpenPlayerInvMessage(curios, name, id);
    }

    public static boolean handle(OpenPlayerInvMessage data, PlayPayloadContext ctx) {
       ctx.workHandler().submitAsync(() -> {
            ClientEvents.openContainerAndScreen(data.windowID, data.otherPlayerName, data.slotAmounts);
        });
        return true;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(windowID);
        buf.writeUtf(otherPlayerName);
        buf.writeInt(slotAmounts.size());
        slotAmounts.forEach((s, i) -> {
            buf.writeUtf(s);
            buf.writeInt(i);
        });
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
