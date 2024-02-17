package com.darkere.crashutils.Network;

import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.DataStructures.TileEntityData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record TileEntityDataMessage( TileEntityData list) implements CustomPacketPayload {
    public static ResourceLocation ID = new ResourceLocation(CrashUtils.MODID,"tileentitydatamessage");

    public static TileEntityDataMessage decode(FriendlyByteBuf buf) {
        return new TileEntityDataMessage(new TileEntityData(NetworkTools.readRLWPMap(buf)));
    }

    public static void handle(TileEntityDataMessage data, PlayPayloadContext ctx) {
       ctx.workHandler().submitAsync(() -> DataHolder.addTileEntityData(data.list));
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
