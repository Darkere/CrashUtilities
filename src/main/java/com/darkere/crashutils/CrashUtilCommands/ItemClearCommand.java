package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.ClearItemTask;
import com.darkere.crashutils.CrashUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;

public class ItemClearCommand implements Command<CommandSource> {

    private static final ItemClearCommand cmd = new ItemClearCommand();

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("callItemClear")
            .executes(cmd);

    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        if (CrashUtils.SERVER_CONFIG.getEnabled()) {
            ClearItemTask.INSTANCE.run();
        } else {
            context.getSource().sendSuccess(new StringTextComponent("ItemClears are not enabled in the config"), false);
        }
        return 1;
    }
}
