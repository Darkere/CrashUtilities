package com.darkere.crashutils.Network;

import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.DataStructures.DataHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Map;
import java.util.Set;

public record LoadedChunkDataTicketsMessage(Map<String, Set<ChunkPos>> data) implements CustomPacketPayload {

    public static ResourceLocation ID = new ResourceLocation(CrashUtils.MODID, "loadedchunkdataticketsmessage");

    public static LoadedChunkDataTicketsMessage decode(FriendlyByteBuf buf) {
        return new LoadedChunkDataTicketsMessage(NetworkTools.readSChPMap(buf));
    }

    public static void handle(LoadedChunkDataTicketsMessage data, PlayPayloadContext ctx) {
       ctx.workHandler().submitAsync(() -> {
            DataHolder.addTicketData(data.data);
        });
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        NetworkTools.writeSChPMap(buf, data);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
