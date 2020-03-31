package com.darkere.crashutils;

import net.minecraft.command.CommandSource;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class TileEntityList {
    Map<ResourceLocation, List<TileEntity>> map = new HashMap<>();
    Map<ResourceLocation, Boolean> tickmap = new HashMap<>();
    int total = 0;
    int totalticking  = 0;

    public TileEntityList() {
        for (Map.Entry<ResourceLocation, TileEntityType<?>> entry : ForgeRegistries.TILE_ENTITIES.getEntries()) {
            map.put(entry.getKey(), new ArrayList<>());
        }
    }

    public void createLists(List<ServerWorld> worlds) {
        List<TileEntity> tileEntities = new ArrayList<>();
        List<TileEntity> ticking = new ArrayList<>();
        worlds.forEach(x -> tileEntities.addAll(x.loadedTileEntityList));
        worlds.forEach(x ->ticking.addAll(x.tickableTileEntities));
        for (TileEntity tileEntity : tileEntities) {
            map.get(tileEntity.getType().getRegistryName()).add(tileEntity);
        }
        total = tileEntities.size();
        for (TileEntity tileEntity : ticking) {
            tickmap.put(tileEntity.getType().getRegistryName(),true);
        }
        totalticking = ticking.size();
    }

    public void reply(ResourceLocation res, CommandSource source) {
        if (res == null) {
            map.entrySet().stream().filter(x->x.getValue().size() != 0).sorted(Comparator.comparingInt(e -> e.getValue().size())).forEach((e) -> {
                CommandUtils.sendFindTEMessage(source, e.getKey(),e.getValue().size(),tickmap.containsKey(e.getKey()));
            });
            CommandUtils.sendNormalMessage(source,total + " TE's , " + totalticking + "ticking", TextFormatting.DARK_AQUA);

        } else {
            map.get(res).forEach(x->  CommandUtils.sendTEMessage(source,x,true));
        }
    }
}
