package com.darkere.crashutils;

import com.google.common.collect.*;
import it.unimi.dsi.fastutil.Hash;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadedChunkList {
    Map<ServerPlayerEntity, Integer> playerChunks = new HashMap<>();
    Map<String,List<ChunkPos>> chunks = new HashMap<>();


    public void addPlayerChunk(ServerPlayerEntity player) {
        int x = playerChunks.getOrDefault(player, 0);
        playerChunks.put(player, x + 1);
    }

    public void addChunk(ChunkPos pos, String ticketname) {
        List<ChunkPos> list = chunks.get(ticketname);
        if(list == null){
            list = new ArrayList<>();
            list.add(pos);
            chunks.put(ticketname,list);
        } else {
            list.add(pos);
        }
    }
    public void reply(CommandSource source){
        chunks.forEach(
            (k,v) -> {
                CommandUtils.sendNormalMessage(source,"Type: " + k  + " Count: " + v.size(), TextFormatting.BLUE);
                StringTextComponent txt = new StringTextComponent("");
                for (ChunkPos chunkPos : v) {
             //       txt.appendSibling(new StringTextComponent(chunkPos.toString()));
                }
                CommandUtils.sendNormalMessage(source,txt.getFormattedText(), TextFormatting.DARK_BLUE);
            }
        );
    }
}
