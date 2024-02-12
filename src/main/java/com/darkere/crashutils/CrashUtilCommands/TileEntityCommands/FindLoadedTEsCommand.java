package com.darkere.crashutils.CrashUtilCommands.TileEntityCommands;

import com.darkere.crashutils.DataStructures.TileEntityData;
import com.darkere.crashutils.WorldUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class FindLoadedTEsCommand implements Command<CommandSourceStack> {
    private static final FindLoadedTEsCommand cmd = new FindLoadedTEsCommand();
    private static final SuggestionProvider<CommandSourceStack> sugg = (ctx, builder) -> SharedSuggestionProvider.suggestResource(ForgeRegistries.BLOCK_ENTITY_TYPES.getKeys().stream(), builder);

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("find")
                .then(Commands.argument("res", ResourceLocationArgument.id())
                        .suggests(sugg)
                        .executes(cmd)
                        .then(Commands.argument("dim", DimensionArgument.dimension())
                                .executes(cmd)));


    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TileEntityData list = new TileEntityData();
        List<ServerLevel> worlds = WorldUtils.getWorldsFromDimensionArgument(context);
        list.createLists(worlds);
        ResourceLocation res = ResourceLocationArgument.getId(context, "res");
        list.reply(res, context.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
