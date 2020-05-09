package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.DataStructures.EntityData;
import com.darkere.crashutils.WorldUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class AllEntitiesCommand implements Command<CommandSource>{
    private static final AllEntitiesCommand cmd = new AllEntitiesCommand();
    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("allEntities")
            .then(Commands.argument("dim", DimensionArgument.getDimension()).executes(cmd))
            .executes(cmd);

    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        EntityData list = new EntityData();
        List<ServerWorld> worlds = WorldUtils.getWorldsFromDimensionArgument(context);
        list.createLists(worlds);
        list.reply(null,context.getSource());
        return Command.SINGLE_SUCCESS;
    }
}
