package com.darkere.crashutils.CrashUtilCommands.EntityCommands;

import com.darkere.crashutils.DataStructures.EntityData;
import com.darkere.crashutils.WorldUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class FindEntitiesCommand implements Command<CommandSource> {

    private static final FindEntitiesCommand cmd = new FindEntitiesCommand();
    private static final SuggestionProvider<CommandSource> sugg = (ctx, builder) -> ISuggestionProvider.suggestResource(ForgeRegistries.ENTITIES.getKeys().stream(), builder);

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("find")
            .then(Commands.argument("res", ResourceLocationArgument.id())
                .suggests(sugg)
                .executes(cmd)
                .then(Commands.argument("dim", DimensionArgument.dimension()))
                .executes(cmd));


    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        EntityData list = new EntityData();
        List<ServerWorld> worlds = WorldUtils.getWorldsFromDimensionArgument(context);
        list.createLists(worlds);
        ResourceLocation res = ResourceLocationArgument.getId(context, "res");
        list.reply(res, context.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
