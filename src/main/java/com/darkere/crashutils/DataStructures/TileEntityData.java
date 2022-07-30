package com.darkere.crashutils.DataStructures;

import com.darkere.crashutils.CommandUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
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
        List<BlockEntity> ticking = new ArrayList<>();
        worlds.forEach(level -> {
            for (ChunkHolder chunk : level.getChunkSource().chunkMap.getChunks()) {
                if(chunk.getTickingChunk() != null)
                    ticking.addAll(chunk.getTickingChunk().getBlockEntities().values());
            }
            for (TickingBlockEntity tileEntity : level.blockEntityTickers) {
                WorldPos pos = WorldPos.getPosFromTileEntity(tileEntity,level);
                TEID.put(pos.getID(), pos);
                map.get(new ResourceLocation(tileEntity.getType())).add(pos);
            }
        });

        total = ticking.size();
    }

    public void reply(ResourceLocation res, CommandSourceStack source) {
        if (res == null) {
            map.entrySet().stream().filter(x -> x.getValue().size() != 0).sorted(Comparator.comparingInt(e -> e.getValue().size())).forEach((e) -> {
                CommandUtils.sendFindTEMessage(source, e.getKey(), e.getValue().size(), tickmap.containsKey(e.getKey()));
            });
            CommandUtils.sendNormalMessage(source, total + " BE's" ,  ChatFormatting.DARK_AQUA);

        } else {
            map.get(res).forEach(x -> CommandUtils.sendTEMessage(source, x, true));
        }
    }


}
