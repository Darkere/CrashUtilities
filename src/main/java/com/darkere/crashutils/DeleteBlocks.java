package com.darkere.crashutils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkDataEvent;

import java.util.HashSet;
import java.util.Set;

public class DeleteBlocks {
    static Set<BlockPos> toDelete = new HashSet<>();

    @SubscribeEvent
    public void loadChunkData(ChunkDataEvent.Load event) {
        toDelete.forEach(x -> {
            if (isInChunk(x, event.getChunk())) {
                deleteBlock(x, event.getChunk());
            }
        });

    }

    public static void addBlockToRemove(BlockPos pos) {
        toDelete.add(pos);
    }

    private boolean isInChunk(BlockPos pos, ChunkAccess chunk) {
        return pos.getX() > chunk.getPos().getMinBlockX() &&
            pos.getX() < chunk.getPos().getMaxBlockX() &&
            pos.getZ() > chunk.getPos().getMinBlockZ() &&
            pos.getZ() < chunk.getPos().getMaxBlockZ();


    }

    private void deleteBlock(BlockPos pos, ChunkAccess chunk) {
        LevelAccessor world = chunk.getLevel();
        if (world != null) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 01);
            //  BlockState block = world.getBlockState(pos);
            //    world.removeBlock(pos,false);
            chunk.removeBlockEntity(pos);
        }
    }
}
