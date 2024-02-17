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

public record LoadedChunkDataStateMessage(Map<String, Set<ChunkPos>> loadedChunkStateData) implements CustomPacketPayload {
    public static ResourceLocation ID = new ResourceLocation(CrashUtils.MODID, "loadedchunkdatastatemessage");

    public static LoadedChunkDataStateMessage decode(FriendlyByteBuf buf) {
        return new LoadedChunkDataStateMessage(NetworkTools.readSChPMap(buf));
    }

    public static void handle(LoadedChunkDataStateMessage data, PlayPayloadContext ctx) {
       ctx.workHandler().submitAsync(() -> {
            DataHolder.addStateData(data.loadedChunkStateData);
        });
    }


    @Override
    public void write(FriendlyByteBuf buf) {
        if (NetworkTools.returnOnNull(loadedChunkStateData)) return;
        NetworkTools.writeSChPMap(buf, loadedChunkStateData);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
