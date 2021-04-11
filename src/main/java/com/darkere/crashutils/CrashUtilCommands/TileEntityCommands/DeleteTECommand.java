package com.darkere.crashutils.CrashUtilCommands.TileEntityCommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;

public class DeleteTECommand implements Command<CommandSource> {


    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("delete")
            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(new DeleteTECommand()));

    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        context.getSource().getLevel().removeBlockEntity(BlockPosArgument.getOrLoadBlockPos(context,"pos"));
        return Command.SINGLE_SUCCESS;
    }
}
