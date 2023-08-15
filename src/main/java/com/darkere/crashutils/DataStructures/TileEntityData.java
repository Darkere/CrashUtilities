package com.darkere.crashutils.DataStructures;

import com.darkere.crashutils.CommandUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class TileEntityData extends LocationData {
    Map<ResourceLocation, Boolean> tickmap = new HashMap<>();
    public static Map<UUID, WorldPos> TEID = new HashMap<>();

    public TileEntityData() {
        for (Map.Entry<ResourceKey<BlockEntityType<?>>, BlockEntityType<?>> entry : ForgeRegistries.BLOCK_ENTITY_TYPES.getEntries()) {
            map.put(entry.getKey().location(), new ArrayList<>());
        }
    }

    public TileEntityData(Map<ResourceLocation, List<WorldPos>> map) {
        this.map = map;
    }

    public void createLists(List<ServerLevel> worlds) {
        for (ServerLevel level : worlds) {
            for (ChunkHolder chunk : level.getChunkSource().chunkMap.getChunks()) {
                if (chunk.getFullStatus().isOrAfter(FullChunkStatus.BLOCK_TICKING)) {
                    chunk.getFullChunk().getBlockEntities().forEach((pos,e) ->{
                        WorldPos wp = new WorldPos(pos, level.dimension(), UUID.randomUUID());
                        TEID.put(wp.getID(), wp);
                        map.get(BlockEntityType.getKey(e.getType())).add(wp);
                    });
                }
            }
        }

       // total = ticking.size();
    }

    public void reply(ResourceLocation res, CommandSourceStack source) {
        if (res == null) {
            map.entrySet().stream().filter(x -> !x.getValue().isEmpty()).sorted(Comparator.comparingInt(e -> e.getValue().size())).forEach((e) -> {
                CommandUtils.sendFindTEMessage(source, e.getKey(), e.getValue().size(), tickmap.containsKey(e.getKey()));
            });
           // CommandUtils.sendNormalMessage(source, total + " BE's" ,  ChatFormatting.DARK_AQUA);

        } else {
            map.get(res).forEach(x -> CommandUtils.sendTEMessage(source, x, true));
        }
    }


}
