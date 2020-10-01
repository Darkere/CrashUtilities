package com.darkere.crashutils.DataStructures;

import com.darkere.crashutils.CommandUtils;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityData extends LocationData {

    public EntityData() {
        for (Map.Entry<RegistryKey<EntityType<?>>, EntityType<?>> entry : ForgeRegistries.ENTITIES.getEntries()) {
            map.put(entry.getKey().getLocation(), new ArrayList<>());
        }
    }

    public EntityData(Map<ResourceLocation, List<WorldPos>> map) {
        this.map = map;
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
            CommandUtils.sendNormalMessage(source, total + " Entities", TextFormatting.DARK_AQUA);

        } else {
            sendEntityChunkMapCommand(source, res);
        }
    }


    private void sendEntityChunkMapCommand(CommandSource source, ResourceLocation res) {
        fillChunkMaps(res.toString());
        CommandUtils.sendNormalMessage(source, res.toString(),TextFormatting.DARK_BLUE);
        chunkMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.comparingInt(List::size))).forEach((k) -> CommandUtils.sendChunkEntityMessage(source, k.getValue().size(), tpPos.get(k.getKey()).pos, tpPos.get(k.getKey()).type, true));
    }
}
