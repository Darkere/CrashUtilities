package com.darkere.crashutils.DataStructures;

import com.darkere.crashutils.CommandUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class EntityData extends LocationData {

    public EntityData() {
        for (Map.Entry<ResourceKey<EntityType<?>>, EntityType<?>> entry : ForgeRegistries.ENTITY_TYPES.getEntries()) {
            map.put(entry.getKey().location(), new ArrayList<>());
        }
    }

    public EntityData(Map<ResourceLocation, List<WorldPos>> map) {
        this.map = map;
    }

    public void createLists(List<ServerLevel> worlds) {
        List<Entity> entities = new ArrayList<>();
        worlds.forEach(x -> x.getEntities().getAll().forEach(entities::add));
        for (Entity entity : entities) {
            map.get(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType())).add(WorldPos.getPosFromEntity(entity));
        }
        total = entities.size();
    }

    public void reply(ResourceLocation res, CommandSourceStack source) {
        if (res == null) {
            map.entrySet().stream().filter(x -> x.getValue().size() != 0).sorted(Comparator.comparingInt(e -> e.getValue().size())).forEach((e) -> {
                CommandUtils.sendFindEMessage(source, e.getKey(), e.getValue().size());
            });
            CommandUtils.sendNormalMessage(source, total + " Entities", ChatFormatting.DARK_AQUA);

        } else {
            sendEntityChunkMapCommand(source, res);
        }
    }


    private void sendEntityChunkMapCommand(CommandSourceStack source, ResourceLocation res) {
        fillChunkMaps(res.toString());
        CommandUtils.sendNormalMessage(source, res.toString(), ChatFormatting.DARK_BLUE);
        chunkMap.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.comparingInt(List::size))).forEach((k) -> CommandUtils.sendChunkEntityMessage(source, k.getValue().size(), tpPos.get(k.getKey()).pos, tpPos.get(k.getKey()).type, true));
    }
}
