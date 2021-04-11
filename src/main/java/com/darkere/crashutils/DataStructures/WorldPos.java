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
        return new WorldPos(new BlockPos(entity.position()), entity.getCommandSenderWorld().dimension(), entity.getUUID());
    }

    public static WorldPos getPosFromTileEntity(TileEntity entity) {
        if (entity.getLevel() == null) return null;
        return new WorldPos(entity.getBlockPos(), entity.getLevel().dimension(), UUID.randomUUID());
    }

    public UUID getID() {
        return id;
    }
}
