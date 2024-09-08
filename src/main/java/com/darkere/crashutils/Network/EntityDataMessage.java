package com.darkere.crashutils.Network;

import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.DataStructures.EntityData;
import com.darkere.crashutils.DataStructures.WorldPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;

public record EntityDataMessage( EntityData entityData)  implements CustomPacketPayload{
   
    // Map<ResourceLocation, List<WorldPos>>
    public static final Type<EntityDataMessage> TYPE = new Type<>(CrashUtils.ResourceLocation("entitydatamessage"));
    public static final StreamCodec< ? super RegistryFriendlyByteBuf, EntityDataMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, WorldPos.STREAM_CODEC.apply(ByteBufCodecs.list())).map(EntityData::new,EntityData::getHashMap),EntityDataMessage::entityData,
            EntityDataMessage::new);

    public static void handle(EntityDataMessage data, IPayloadContext ctx) {
            DataHolder.addEntityData(data.entityData);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
