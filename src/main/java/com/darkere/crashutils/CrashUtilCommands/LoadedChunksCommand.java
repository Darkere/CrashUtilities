package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.DataStructures.LoadedChunkData;
import com.darkere.crashutils.WorldUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public class LoadedChunksCommand {


    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("chunks")
            .then(Commands.argument("dim", DimensionArgument.getDimension()).executes(x -> run(x, 0, null)))
            .executes(x -> run(x, 0, null))
            .then(Commands.literal("byLocation")
                .then(Commands.argument("loc", StringArgumentType.word())
                    .executes(x -> run(x, 1, StringArgumentType.getString(x, "loc")))))
            .then(Commands.literal("byTicket")
                .then(Commands.argument("tic", StringArgumentType.word())
                    .executes(x -> run(x, 2, StringArgumentType.getString(x, "tic")))));
    }


    public static int run(CommandContext<CommandSource> context, int type, String word) throws CommandSyntaxException {
        List<ServerWorld> worlds = WorldUtils.getWorldsFromDimensionArgument(context);
        LoadedChunkData loadedChunkData = new LoadedChunkData(worlds);
        CrashUtils.runNextTick((world) -> reply(type, loadedChunkData, context, word));
        return Command.SINGLE_SUCCESS;
    }

    public static void reply(int type, LoadedChunkData loadedChunkData, CommandContext<CommandSource> context, String word) {
        try {
            switch (type) {
                case 0: {
                    loadedChunkData.reply(context.getSource());
                    break;
                }
                case 1: {
                    loadedChunkData.replyWithLocation(context.getSource(), word);
                    break;
                }
                case 2: {
                    loadedChunkData.replyWithTicket(context.getSource(), word);
                    break;
                }
            }
        } catch (CommandSyntaxException e) {
            context.getSource().sendFeedback(new StringTextComponent("Exception getting player"), true);
        }

    }
}
