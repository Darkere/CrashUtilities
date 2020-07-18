package com.darkere.crashutils.DataStructures;

import com.darkere.crashutils.CommandUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.stream.Collectors;

public class EntityData {
    Map<ResourceLocation, List<WorldPos>> map = new HashMap<>();
    Map<ChunkPos, Integer> chunkMap = new HashMap<>();
    Map<ChunkPos, WorldPos> tpPos = new HashMap<>();
    int total = 0;

    public EntityData() {
        for (Map.Entry<ResourceLocation, EntityType<?>> entry : ForgeRegistries.ENTITIES.getEntries()) {
            map.put(entry.getKey(), new ArrayList<>());
        }
    }

    public EntityData(Map<ResourceLocation, List<WorldPos>> map) {
        this.map = map;
    }

    public Map<ResourceLocation, List<WorldPos>> getMap() {
        return map;
    }

    public Map<ChunkPos, Integer> getChunkMap() {
        return chunkMap;
    }

    public void createLists(List<ServerWorld> worlds) {
        List<Entity> entities = new ArrayList<>();
        worlds.forEach(x -> entities.addAll(x.getEntities().collect(Collectors.toList())));
        for (Entity entity : entities) {
            map.get(entity.getType().getRegistryName()).add(WorldPos.getPosFromEntity(entity));
        }
        total = entities.size();
    }

    public void reply(ResourceLocation res, CommandSource source) {
        if (res == null) {
            map.entrySet().stream().filter(x -> x.getValue().size() != 0).sorted(Comparator.comparingInt(e -> e.getValue().size())).forEach((e) -> {
                CommandUtils.sendFindEMessage(source, e.getKey(), e.getValue().size());
            });
            CommandUtils.sendNormalMessage(source, total + " Entities", Color.func_240744_a_(TextFormatting.DARK_AQUA));

        } else {
            createEntityChunkMap(source, res);
        }
    }

    public void fillChunkMap(ResourceLocation rl) {
        TileEntityData.fillChunkMaps(rl, map, chunkMap, tpPos);
    }

    private void createEntityChunkMap(CommandSource source, ResourceLocation res) {
        fillChunkMap(res);
        CommandUtils.sendNormalMessage(source, res.toString(), Color.func_240744_a_(TextFormatting.DARK_BLUE));
        chunkMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Integer::compareTo)).forEach((k) -> CommandUtils.sendChunkEntityMessage(source, k.getValue(), tpPos.get(k.getKey()).pos, tpPos.get(k.getKey()).type, true));


    }

    public int getEntityCountForChunk(ChunkPos chunkPos) {
        if (chunkMap == null) return 0;
        Integer i = chunkMap.get(chunkPos);
        return i == null ? 0 : i;
    }

    public WorldPos getTpforChunk(ChunkPos chunkfromString) {
        return tpPos.get(chunkfromString);
    }
}
