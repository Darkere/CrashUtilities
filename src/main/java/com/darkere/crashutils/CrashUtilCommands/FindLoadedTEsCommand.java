package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.DataStructures.TileEntityData;
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

public class FindLoadedTEsCommand implements Command<CommandSource> {
    private static final FindLoadedTEsCommand cmd = new FindLoadedTEsCommand();
    private static final SuggestionProvider<CommandSource> sugg = (ctx,builder) -> ISuggestionProvider.func_212476_a(ForgeRegistries.TILE_ENTITIES.getKeys().stream(), builder);
    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("findLoadedTileEntities")
            .then(Commands.argument("res", ResourceLocationArgument.resourceLocation())
                .suggests(sugg)
                .executes(cmd)
            )
            .then(Commands.argument("dim", DimensionArgument.getDimension())
                .executes(cmd));


    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        TileEntityData list = new TileEntityData();
        List<ServerWorld> worlds = WorldUtils.getWorldsFromDimensionArgument(context);
        list.createLists(worlds);
        ResourceLocation res = ResourceLocationArgument.getResourceLocation(context,"res");
        list.reply(res,context.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
