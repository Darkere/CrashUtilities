package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.DeleteBlocks;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;

public class DeleteBlockCommand implements Command<CommandSource> {




    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("deleteTE").then(Commands.argument("pos",BlockPosArgument.blockPos()).executes(new DeleteBlockCommand()));

    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        DeleteBlocks.addBlockToRemove(BlockPosArgument.getLoadedBlockPos(context,"pos"));
        return Command.SINGLE_SUCCESS;
    }
}
