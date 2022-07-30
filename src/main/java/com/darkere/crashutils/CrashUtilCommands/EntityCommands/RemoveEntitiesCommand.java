package com.darkere.crashutils.CrashUtilCommands.EntityCommands;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.WorldUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collections;
import java.util.List;

public class RemoveEntitiesCommand {
    private static final SuggestionProvider<CommandSourceStack> sugg = (ctx, builder) -> SharedSuggestionProvider.suggestResource(ForgeRegistries.ENTITY_TYPES.getKeys().stream(), builder);
    private static final SuggestionProvider<CommandSourceStack> boolsugg = (ctx, builder) -> SharedSuggestionProvider.suggest(Collections.singletonList("force"), builder);
    private static int counter = 0;

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("remove")
            .executes(ctx -> removeEntities(ctx, null))
            .then(Commands.literal("byType")
                .then(Commands.argument("type", ResourceLocationArgument.id())
                    .suggests(sugg)
                    .executes(ctx -> removeEntities(ctx, ResourceLocationArgument.getId(ctx, "type")))
                    .then(Commands.argument("force", StringArgumentType.word())
                        .suggests(boolsugg)
                        .executes(ctx -> removeEntities(ctx, ResourceLocationArgument.getId(ctx, "type"))))))
            .then(Commands.argument("type", ResourceLocationArgument.id())
                .suggests(sugg)
                .executes(ctx -> removeEntities(ctx, ResourceLocationArgument.getId(ctx, "type")))
                .then(Commands.argument("force", StringArgumentType.word())
                    .suggests(boolsugg)
                    .executes(ctx -> removeEntities(ctx, ResourceLocationArgument.getId(ctx, "type")))))
            .then(Commands.literal("items")
                .executes(ctx -> removeItems(ctx, null))
                .then(Commands.argument("force", StringArgumentType.word())
                    .suggests(boolsugg)
                    .executes(ctx -> removeItems(ctx, null)))
                .then(Commands.argument("name", StringArgumentType.word())
                    .executes(ctx -> removeItems(ctx, StringArgumentType.getString(ctx, "name")))
                    .then(Commands.argument("force", StringArgumentType.word())
                        .suggests(boolsugg)
                        .executes(ctx -> removeItems(ctx, StringArgumentType.getString(ctx, "name"))))))
            .then(Commands.literal("hostile")
                .executes(RemoveEntitiesCommand::removeMonsters)
                .then(Commands.argument("force", StringArgumentType.word())
                    .suggests(boolsugg)
                    .executes(RemoveEntitiesCommand::removeMonsters)))
            .then(Commands.literal("regex")
                .then(Commands.argument("regex", StringArgumentType.greedyString())
                    .executes(ctx -> removeEntitiesByRegEx(ctx, StringArgumentType.getString(ctx, "regex")))
                    .then(Commands.argument("force", StringArgumentType.word())
                        .suggests(boolsugg)
                        .executes(ctx -> removeEntitiesByRegEx(ctx, StringArgumentType.getString(ctx, "regex"))))));


    }

    private static int removeEntities(CommandContext<CommandSourceStack> context, ResourceLocation type) {
        counter = 0;
        List<ServerLevel> worlds = WorldUtils.getWorldsFromDimensionArgument(context);
        worlds.forEach(world -> world.getEntities().getAll().forEach(entity -> {
            if (type == null) {
                if(!entity.hasCustomName())
                    entity.remove(Entity.RemovalReason.DISCARDED);
            } else {
                var resourceLocation = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
                if (resourceLocation != null && resourceLocation.equals(type)) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        }));
        respond(context);
        return 1;
    }

    private static int removeEntitiesByRegEx(CommandContext<CommandSourceStack> context, String regex) {
        counter = 0;
        List<ServerLevel> worlds = WorldUtils.getWorldsFromDimensionArgument(context);
        worlds.forEach(world -> world.getEntities().getAll().forEach(entity -> {
            boolean remove = false;
            if (regex == null) {
                remove = !entity.hasCustomName();
            } else {
                var resourceLocation = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
                if (resourceLocation != null) {
                    remove = resourceLocation.toString().matches(regex);
                }
            }
            if (remove)
                entity.remove(Entity.RemovalReason.DISCARDED);
        }));
        respond(context);
        return 1;
    }

    private static int removeItems(CommandContext<CommandSourceStack> context, String type) {
        counter = 0;
        List<ServerLevel> worlds = WorldUtils.getWorldsFromDimensionArgument(context);
        worlds.forEach(world -> world.getEntities().getAll().forEach(entity -> {
            if (entity instanceof ItemEntity) {
                if (type == null)
                    entity.remove(Entity.RemovalReason.DISCARDED);
                else if (entity.getName().getString().contains(type))
                    entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }));
        respond(context);
        return 1;
    }

    private static int removeMonsters(CommandContext<CommandSourceStack> context) {
        counter = 0;
        List<ServerLevel> worlds = WorldUtils.getWorldsFromDimensionArgument(context);
        worlds.forEach(world -> world.getEntities().getAll().forEach(entity -> {
            if (entity.getType().getCategory() == MobCategory.MONSTER && !entity.hasCustomName())
                entity.remove(Entity.RemovalReason.DISCARDED);
        }));
        respond(context);
        return 1;
    }

    private static void respond(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(CommandUtils.CreateTextComponent("Removed " + counter + " Entities"), true);
    }
}
