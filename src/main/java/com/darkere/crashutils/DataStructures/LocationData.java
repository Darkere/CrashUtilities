package com.darkere.crashutils.DataStructures;

import com.darkere.crashutils.Screens.CUOption;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class LocationData {
    Map<ChunkPos, List<WorldPos>> chunkMap = new HashMap<>();
    Map<ChunkPos, WorldPos> tpPos = new HashMap<>();
    Map<ResourceLocation, List<WorldPos>> map = new HashMap<>();
    int total = 0;
    String lastFill = "";

    public LocationData(Map<ResourceLocation, List<WorldPos>> map) {
        this.map = map;
    }

    public LocationData() {
    }

    public Map<ResourceLocation, List<WorldPos>> getMap() {
        return map;
    }
    public HashMap<ResourceLocation, List<WorldPos>> getHashMap() {
        return (HashMap<ResourceLocation, List<WorldPos>>) map;
    }

    public void fillChunkMaps(String rl) {
        if (lastFill.equals(rl)) return;
        lastFill = rl;
        ResourceLocation resourceLocation = ResourceLocation.parse(rl);
        chunkMap.clear();
        tpPos.clear();
        List<WorldPos> positions = new ArrayList<>();
        if (rl.isEmpty()) {
            map.values().forEach(positions::addAll);
        } else {
            positions.addAll(map.get(resourceLocation));
        }
        for (WorldPos pos : positions) {
            ChunkPos chunkPos = new ChunkPos(pos.pos());
            if (!chunkMap.containsKey(chunkPos)) {
                chunkMap.put(chunkPos, new ArrayList<>());
            }
            chunkMap.get(chunkPos).add(pos);
            tpPos.put(chunkPos, pos);
        }
    }

    public int getCountForChunk(ChunkPos chunkPos, String filter) {
        if (chunkMap == null) return 0;
        fillChunkMaps(filter);
        if (chunkMap.get(chunkPos) == null) return 0;
        return chunkMap.get(chunkPos).size();
    }

    public List<CUOption> getAsCUOptions() {
        List<CUOption> list = new ArrayList<>();
        map.forEach((type, chunks) -> {
            if (!chunks.isEmpty()) list.add(new CUOption(type, chunks.size()));
        });
        return list;
    }

    public List<CUOption> getAsCUOptionsOfType(ResourceLocation name) {
        List<CUOption> list = new ArrayList<>();
        fillChunkMaps(name.toString());
        chunkMap.forEach((chunk, listPos) -> {
            if (!listPos.isEmpty()) list.add(new CUOption(chunk, listPos.size(), name));
        });
        return list;
    }


    public List<CUOption> getInChunkAsCUOptions(ChunkPos chunkPos, ResourceLocation name) {
        List<CUOption> list = new ArrayList<>();
        fillChunkMaps(name.toString());
        chunkMap.get(chunkPos).forEach(pos -> list.add(new CUOption(pos.pos(), pos.id())));
        return list;
    }

    public void resetChunkMap() {
        lastFill = "THISWILLRELOADTHECHUNKMAP";
    }
}
