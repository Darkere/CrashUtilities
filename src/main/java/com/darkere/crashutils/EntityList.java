package com.darkere.crashutils;

import com.mojang.brigadier.Command;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class EntityList {
    Map<ResourceLocation, List<Entity>> map = new HashMap<>();
    int total = 0;

    public EntityList() {
        for (Map.Entry<ResourceLocation, EntityType<?>> entry : ForgeRegistries.ENTITIES.getEntries()) {
            map.put(entry.getKey(), new ArrayList<>());
        }
    }

    public void createLists(List<ServerWorld> worlds) {
        List<Entity> entities = new ArrayList<>();
        worlds.forEach(x -> entities.addAll(x.getEntities().collect(Collectors.toList())));
        for (Entity entity : entities) {
            map.get(entity.getType().getRegistryName()).add(entity);
        }
        total = entities.size();
    }

    public void reply(ResourceLocation res, CommandSource source) {
        if (res == null) {
            map.entrySet().stream().filter(x->x.getValue().size() != 0).sorted(Comparator.comparingInt(e -> e.getValue().size())).forEach((e) -> {
                CommandUtils.sendFindEMessage(source, e.getKey(),e.getValue().size());
            });
            CommandUtils.sendNormalMessage(source,total + " Entities" , TextFormatting.DARK_AQUA);

        } else {
           createEntityChunkMap(source, res);
        }
    }
    private void createEntityChunkMap(CommandSource source, ResourceLocation res){
        List<Entity> list = map.get(res);
        Map<ChunkPos,Integer> chunkMap = new HashMap<>();
        Map<ChunkPos,Entity> tpPos = new HashMap<>();
        for (Entity entity : list) {
            ChunkPos pos = new ChunkPos(entity.getPosition());
            if(chunkMap.containsKey(pos)){
                int x = chunkMap.get(pos);
                x++;
                chunkMap.put(pos,x);
            } else {
                chunkMap.put(pos,1);
                tpPos.put(pos,entity);
            }
        }
        CommandUtils.sendNormalMessage(source,res.toString(), TextFormatting.DARK_BLUE);
        chunkMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Integer::compareTo)).forEach((k) -> CommandUtils.sendChunkEntityMessage(source,k.getValue(),tpPos.get(k.getKey()).getPosition(),tpPos.get(k.getKey()).getEntityWorld().getDimension().getType(),true));


    }
}
