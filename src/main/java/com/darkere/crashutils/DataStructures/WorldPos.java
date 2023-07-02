package com.darkere.crashutils.DataStructures;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

import java.util.UUID;

public class WorldPos {
    public BlockPos pos;
    public ResourceKey<Level> type;
    public UUID id;

    public WorldPos(BlockPos pos, ResourceKey<Level> type, UUID id) {
        this.pos = pos;
        this.type = type;
        this.id = id;
    }

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
