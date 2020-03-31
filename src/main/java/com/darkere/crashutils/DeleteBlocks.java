package com.darkere.crashutils;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.Set;

public class DeleteBlocks {
    static Set<BlockPos> toDelete = new HashSet<>();
    @SubscribeEvent
    public void loadChunkData(ChunkDataEvent.Load event){
        toDelete.forEach(x -> {
            if(isInChunk(x,event.getChunk())){
                deleteBlock(x,event.getChunk());
            }
        });

    }
    public static void addBlockToRemove(BlockPos pos){
        toDelete.add(pos);
    }
    private boolean isInChunk(BlockPos pos, IChunk chunk){
        return pos.getX() > chunk.getPos().getXStart() &&
            pos.getX() < chunk.getPos().getXEnd() &&
            pos.getZ() > chunk.getPos().getZStart() &&
            pos.getZ() < chunk.getPos().getZEnd();


    }
    private void deleteBlock(BlockPos pos,IChunk chunk){
        IWorld world = chunk.getWorldForge();
        if(world != null){
          //  BlockState block = world.getBlockState(pos);
        //    world.removeBlock(pos,false);
            chunk.removeTileEntity(pos);
            System.out.println("TE GONE ");
        }
    }
}
