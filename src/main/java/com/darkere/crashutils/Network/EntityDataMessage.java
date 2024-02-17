package com.darkere.crashutils.Network;

import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.DataStructures.EntityData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record EntityDataMessage( EntityData list) implements CustomPacketPayload {
   
    static ResourceLocation ID = new ResourceLocation(CrashUtils.MODID,"entitydatamessage");

    public static EntityDataMessage decode(FriendlyByteBuf buf) {
        return new EntityDataMessage(new EntityData(NetworkTools.readRLWPMap(buf)));
    }

    public static void handle(EntityDataMessage data, PlayPayloadContext ctx) {
        
       ctx.workHandler().submitAsync(() -> {
            DataHolder.addEntityData(data.list);
        });
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        NetworkTools.writeRLWPMap(list.getMap(), buf);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
