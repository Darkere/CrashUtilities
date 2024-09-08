package com.darkere.crashutils.Network;

import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.DataStructures.DataHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.*;

public record LoadedChunkDataStateMessage(Map<String, Set<ChunkPos>> loadedChunkStateData) implements CustomPacketPayload {
    public static final Type<LoadedChunkDataStateMessage> TYPE = new Type<>(CrashUtils.ResourceLocation( "loadedchunkdatastatemessage"));
    public static final StreamCodec<? super RegistryFriendlyByteBuf,LoadedChunkDataStateMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new,ByteBufCodecs.STRING_UTF8,ByteBufCodecs.VAR_LONG.map(ChunkPos::new,ChunkPos::toLong).apply(ByteBufCodecs.list()).map(HashSet::new,x -> x.stream().toList())),
            LoadedChunkDataStateMessage::loadedChunkStateData,
                    LoadedChunkDataStateMessage::new);


    public static void handle(LoadedChunkDataStateMessage data, IPayloadContext ctx) {
            DataHolder.addStateData(data.loadedChunkStateData);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
