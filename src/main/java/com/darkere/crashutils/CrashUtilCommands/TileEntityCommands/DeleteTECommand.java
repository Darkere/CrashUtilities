package com.darkere.crashutils.CrashUtilCommands.TileEntityCommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;

public class DeleteTECommand implements Command<CommandSourceStack> {


    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("delete")
            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(new DeleteTECommand()));

    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        context.getSource().getLevel().removeBlockEntity(BlockPosArgument.getLoadedBlockPos(context,"pos"));
        return Command.SINGLE_SUCCESS;
    }
}
