package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.ClearItemTask;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class ItemClearCommand implements Command<CommandSource> {

    private static final ItemClearCommand cmd = new ItemClearCommand();
    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("callItemClear").
            executes(cmd);

    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ClearItemTask.scheduled = true;
        return 1;
    }
}
