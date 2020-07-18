package com.darkere.crashutils.DataStructures;

import com.darkere.crashutils.CommandUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class TileEntityData {
    Map<ResourceLocation, List<WorldPos>> map = new HashMap<>();
    Map<ResourceLocation, Boolean> tickmap = new HashMap<>();
    Map<ChunkPos, Integer> chunkMap = new HashMap<>();
    Map<ChunkPos, WorldPos> tpPos = new HashMap<>();
    int total = 0;
    int totalticking = 0;

    public TileEntityData() {
        for (Map.Entry<ResourceLocation, TileEntityType<?>> entry : ForgeRegistries.TILE_ENTITIES.getEntries()) {
            map.put(entry.getKey(), new ArrayList<>());
        }
    }

    public TileEntityData(Map<ResourceLocation, List<WorldPos>> map) {
        this.map = map;
    }

    public void createLists(List<ServerWorld> worlds) {
        List<TileEntity> tileEntities = new ArrayList<>();
        List<TileEntity> ticking = new ArrayList<>();
        worlds.forEach(x -> tileEntities.addAll(x.loadedTileEntityList));
        worlds.forEach(x -> ticking.addAll(x.tickableTileEntities));
        for (TileEntity tileEntity : tileEntities) {
            map.get(tileEntity.getType().getRegistryName()).add(WorldPos.getPosFromTileEntity(tileEntity));
        }
        total = tileEntities.size();
        for (TileEntity tileEntity : ticking) {
            tickmap.put(tileEntity.getType().getRegistryName(), true);
        }
        totalticking = ticking.size();
    }

    public void reply(ResourceLocation res, CommandSource source) {
        if (res == null) {
            map.entrySet().stream().filter(x -> x.getValue().size() != 0).sorted(Comparator.comparingInt(e -> e.getValue().size())).forEach((e) -> {
                CommandUtils.sendFindTEMessage(source, e.getKey(), e.getValue().size(), tickmap.containsKey(e.getKey()));
            });
            CommandUtils.sendNormalMessage(source, total + " TE's , " + totalticking + "ticking", Color.func_240744_a_( TextFormatting.DARK_AQUA));

        } else {
            map.get(res).forEach(x -> CommandUtils.sendTEMessage(source, x, true));
        }
    }

    public Map<ResourceLocation, List<WorldPos>> getMap() {
        return map;
    }

    public void fillChunkMap(ResourceLocation rl) {
        fillChunkMaps(rl, map, chunkMap, tpPos);
    }

    static void fillChunkMaps(ResourceLocation rl, Map<ResourceLocation, List<WorldPos>> map, Map<ChunkPos, Integer> chunkMap, Map<ChunkPos, WorldPos> tpPos) {
        List<WorldPos> entities = new ArrayList<>();
        if (rl == null) {
            for (ResourceLocation resourceLocation : map.keySet()) {
                entities.addAll(map.get(resourceLocation));
            }
        } else {
            entities = map.get(rl);
        }
        chunkMap.clear();
        tpPos.clear();
        for (WorldPos entity : entities) {
            ChunkPos pos = new ChunkPos(entity.pos);
            if (chunkMap.containsKey(pos)) {
                int x = chunkMap.get(pos);
                x++;
                chunkMap.put(pos, x);
            } else {
                chunkMap.put(pos, 1);
                tpPos.put(pos, entity);
            }
        }
    }

    public int getTileEntityCountForChunk(ChunkPos chunkPos) {
        if (chunkMap == null) return 0;
        Integer i = chunkMap.get(chunkPos);
        return i == null ? 0 : i;
    }

    public Map<ChunkPos, Integer> getChunkMap() {
        return chunkMap;
    }

    public WorldPos getTpforChunk(ChunkPos pos) {
        return tpPos.get(pos);
    }
}
