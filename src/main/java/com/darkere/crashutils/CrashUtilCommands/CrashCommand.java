package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.CrashUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

import java.util.logging.LogManager;

public class CrashCommand implements Command<CommandSource> {
    private static final CrashCommand cmd = new CrashCommand();
    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("crashTheGameWithoutSave").
          executes(cmd);

    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        LogManager.getLogManager().getLogger(CrashUtils.MODID).warning("CRASHING THE GAME");
        Runtime.getRuntime().halt(99);
        return Command.SINGLE_SUCCESS;
    }
}
