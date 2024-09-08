package com.darkere.crashutils.Network;

import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.DataStructures.DataHolder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public record LoadedChunkDataTicketsMessage(Map<String, Set<ChunkPos>> data) implements CustomPacketPayload {

    public static final Type<LoadedChunkDataTicketsMessage> TYPE = new Type<>(CrashUtils.ResourceLocation( "loadedchunkdataticketsmessage"));
    public static final StreamCodec<? super RegistryFriendlyByteBuf, LoadedChunkDataTicketsMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new,ByteBufCodecs.STRING_UTF8,ByteBufCodecs.VAR_LONG.map(ChunkPos::new,ChunkPos::toLong).apply(ByteBufCodecs.list()).map(HashSet::new, x -> x.stream().toList())),
            LoadedChunkDataTicketsMessage::data,
            LoadedChunkDataTicketsMessage::new
    );

    public static void handle(LoadedChunkDataTicketsMessage data, IPayloadContext ctx) {
            DataHolder.addTicketData(data.data);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
