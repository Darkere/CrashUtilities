package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.ClearItemTask;
import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;

public class ItemClearCommand implements Command<CommandSourceStack> {

    private static final ItemClearCommand cmd = new ItemClearCommand();

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("callItemClear")
            .executes(cmd);

    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (CrashUtils.SERVER_CONFIG.getEnabled()) {
            ClearItemTask.INSTANCE.run();
            CrashUtils.runNextTick((world)->{
                world.getServer().getPlayerList().broadcastSystemMessage(CommandUtils.CreateTextComponent(ClearItemTask.INSTANCE.lastCount + " Item Entities in World. Limit is set to " + CrashUtils.SERVER_CONFIG.getMaximum()), ChatType.SYSTEM);
            });
        } else {
            context.getSource().sendSuccess(CommandUtils.CreateTextComponent("ItemClears are not enabled in the config"), false);
        }
        return 1;
    }
}
