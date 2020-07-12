package com.darkere.crashutils.DataStructures;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;

public class WorldPos {
    public BlockPos pos;
    public DimensionType type;

    public WorldPos(BlockPos pos, DimensionType type) {
        this.pos = pos;
        this.type = type;
    }

    public static WorldPos getPosFromEntity(Entity entity) {
        return new WorldPos(entity.getPosition(), entity.getEntityWorld().getDimension().getType());
    }

    public static WorldPos getPosFromTileEntity(TileEntity entity) {
        if (entity.getWorld() == null) return null;
        return new WorldPos(entity.getPos(), entity.getWorld().getDimension().getType());
    }
}
