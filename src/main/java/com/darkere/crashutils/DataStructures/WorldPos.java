package com.darkere.crashutils.DataStructures;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

import java.util.UUID;

public record WorldPos(BlockPos pos, ResourceKey<Level> type, UUID id) {
    public static StreamCodec<ByteBuf, WorldPos> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, WorldPos::pos,
            ResourceKey.streamCodec(Registries.DIMENSION), WorldPos::type,
            UUIDUtil.STREAM_CODEC, WorldPos::id,
            WorldPos::new
    );

    public static WorldPos getPosFromEntity(Entity entity) {
        return new WorldPos(entity.getOnPos(), entity.getCommandSenderWorld().dimension(), entity.getUUID());
    }

    public static WorldPos getPosFromTileEntity(TickingBlockEntity entity, Level level) {
        return new WorldPos(entity.getPos(), level.dimension(), UUID.randomUUID());
    }

    public UUID getID() {
        return id;
    }
}
