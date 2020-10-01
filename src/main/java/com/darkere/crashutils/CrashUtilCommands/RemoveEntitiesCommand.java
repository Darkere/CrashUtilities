package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.WorldUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.List;

public class RemoveEntitiesCommand {
    private static final SuggestionProvider<CommandSource> sugg = (ctx, builder) -> ISuggestionProvider.func_212476_a(ForgeRegistries.ENTITIES.getKeys().stream(), builder);
    private static final SuggestionProvider<CommandSource> boolsugg = (ctx, builder) -> ISuggestionProvider.suggest(Arrays.asList("byForce", "normally"), builder);
    private static int counter = 0;

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("removeEntities")
            .then(Commands.argument("force", StringArgumentType.word())
                .suggests(boolsugg)
                .executes(ctx -> removeEntities(ctx, null))
                .then(Commands.literal("byType")
                    .then(Commands.argument("type", ResourceLocationArgument.resourceLocation())
                        .suggests(sugg)
                        .executes(ctx -> removeEntities(ctx, ResourceLocationArgument.getResourceLocation(ctx, "type")))))
                .then(Commands.literal("items")
                    .executes(ctx -> removeItems(ctx, null))
                    .then(Commands.argument("name", StringArgumentType.word())
                        .executes(ctx -> removeItems(ctx, StringArgumentType.getString(ctx, "name")))))
                .then(Commands.literal("hostile")
                    .executes(RemoveEntitiesCommand::removeMonsters)));


    }

    private static int removeEntities(CommandContext<CommandSource> context, ResourceLocation type) {
        counter = 0;
        List<ServerWorld> worlds = WorldUtils.getWorldsFromDimensionArgument(context);
        worlds.forEach(world -> world.getEntities().filter(entity -> {
            if (type == null) {
                return !entity.hasCustomName();
            } else {
                if (entity.getType().getRegistryName() != null) {
                    return entity.getType().getRegistryName().equals(type);
                }
            }
            return false;
        }).forEach(x -> removeEntity(context, world, x)));
        respond(context);
        return 1;
    }

    private static int removeItems(CommandContext<CommandSource> context, String type) {
        counter = 0;
        List<ServerWorld> worlds = WorldUtils.getWorldsFromDimensionArgument(context);
        worlds.forEach(world -> world.getEntities().filter(x -> x instanceof ItemEntity).map(x -> (ItemEntity) x).filter(x -> type == null ? !x.hasCustomName() : x.getName().getString().contains(type)).forEach(x -> removeEntity(context, world, x)));
        respond(context);
        return 1;
    }

    private static int removeMonsters(CommandContext<CommandSource> context) {
        counter = 0;
        List<ServerWorld> worlds = WorldUtils.getWorldsFromDimensionArgument(context);
        worlds.forEach(world -> world.getEntities().filter(x -> x.getType().getClassification() == EntityClassification.MONSTER).forEach(x -> removeEntity(context, world, x)));
        respond(context);
        return 1;
    }

    private static void removeEntity(CommandContext<CommandSource> context, ServerWorld world, Entity x) {
        String forced = StringArgumentType.getString(context, "force");
        boolean force = forced.equals("byForce");
        if (force) {
            world.removeEntityComplete(x, false);
        } else {
            x.remove();
        }
        counter++;
    }

    private static void respond(CommandContext<CommandSource> context) {
        context.getSource().sendFeedback(new StringTextComponent("Removed " + counter + " Entities"), true);
    }
}
