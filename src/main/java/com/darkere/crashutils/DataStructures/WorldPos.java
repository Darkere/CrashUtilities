package com.darkere.crashutils.DataStructures;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.UUID;

public class WorldPos {
    public BlockPos pos;
    public RegistryKey<World> type;
    public UUID id;

    public WorldPos(BlockPos pos, RegistryKey<World> type, UUID id) {
        this.pos = pos;
        this.type = type;
        this.id = id;
    }

    public static WorldPos getPosFromEntity(Entity entity) {
        return new WorldPos(new BlockPos(entity.getPositionVec()), entity.getEntityWorld().getDimensionKey(), entity.getUniqueID());
    }

    public static WorldPos getPosFromTileEntity(TileEntity entity) {
        if (entity.getWorld() == null) return null;
        return new WorldPos(entity.getPos(), entity.getWorld().getDimensionKey(), UUID.randomUUID());
    }

    public UUID getID() {
        return id;
    }
}
