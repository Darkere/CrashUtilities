package com.darkere.crashutils.CrashUtilCommands.TileEntityCommands;

import com.darkere.crashutils.DataStructures.TileEntityData;
import com.darkere.crashutils.WorldUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public class AllLoadedTEsCommand implements Command<CommandSourceStack> {
    private static final AllLoadedTEsCommand cmd = new AllLoadedTEsCommand();

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("entityData")
                .then(Commands.argument("dim", DimensionArgument.dimension())
                        .executes(cmd))
                .executes(cmd);

    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TileEntityData list = new TileEntityData();
        List<ServerLevel> worlds = WorldUtils.getWorldsFromDimensionArgument(context);
        list.createLists(worlds);
        list.reply(null, context.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
