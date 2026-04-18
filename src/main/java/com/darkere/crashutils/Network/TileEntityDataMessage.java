package com.darkere.crashutils.Network;

import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.DataStructures.TileEntityData;
import com.darkere.crashutils.DataStructures.WorldPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;

public record TileEntityDataMessage( TileEntityData list) implements CustomPacketPayload {
    public static final Type<TileEntityDataMessage> TYPE = new Type<>(CrashUtils.Identifier("tileentitydatamessage"));
    public static final StreamCodec<? super RegistryFriendlyByteBuf, TileEntityDataMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, WorldPos.STREAM_CODEC.apply(ByteBufCodecs.list())).map(TileEntityData::new,TileEntityData::getHashMap),TileEntityDataMessage::list,
            TileEntityDataMessage::new
    );

    public static void handle(TileEntityDataMessage data, IPayloadContext ctx) {
       DataHolder.addTileEntityData(data.list);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
