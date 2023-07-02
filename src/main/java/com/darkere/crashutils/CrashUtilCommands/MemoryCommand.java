package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.MemoryChecker;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class MemoryCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("memoryCheck")
            .executes(ctx -> run(ctx, 10))
            .then(Commands.argument("count", IntegerArgumentType.integer())
                .executes(ctx -> run(ctx, IntegerArgumentType.getInteger(ctx, "count"))));
    }

    private static int run(CommandContext<CommandSourceStack> context, int count) {
        if (!CrashUtils.SERVER_CONFIG.getMemoryChecker()) {
            context.getSource().sendSuccess(()->CommandUtils.CreateTextComponent("Memory Checker not enabled in Config"), true);
            return 0;
        }
        List<MemoryChecker.MemoryCount> full = MemoryChecker.INSTANCE.counts;
        if (full.size() < count) {
            count = full.size();
        }
        for (int i = full.size() - count; i < full.size(); i++) { //last count elements
            int finalI = i;
            context.getSource().sendSuccess(()->createVisualMemoryText(full.get(finalI)), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static Component createVisualMemoryText(MemoryChecker.MemoryCount count) {
        MutableComponent text = CommandUtils.CreateTextComponent("[");
        double maximum = (Math.ceil(MemoryChecker.inGigaBytes(count.getMaximum())));
        double total = MemoryChecker.inGigaBytes(count.getTotal());
        double used = total - MemoryChecker.inGigaBytes(count.getFree());
        double percentTotal = total / maximum;
        double percentUsed = used / maximum;

        for (double i = 0.1D; i <= 1; i += 0.1D) {
            if (i < percentUsed) {
                text.append(CommandUtils.coloredComponent("I", ChatFormatting.RED));
            } else if (i < percentTotal) {
                text.append(CommandUtils.coloredComponent("I", ChatFormatting.YELLOW));
            } else {
                text.append(CommandUtils.coloredComponent("I", ChatFormatting.GREEN));
            }
        }
        int usedpercent = (int) (percentUsed * 100);
        int allocatedpercent = (int) (percentTotal * 100);
        text.append(CommandUtils.coloredComponent("] " + usedpercent + " % Used " + allocatedpercent + " % Allocated", ChatFormatting.WHITE));

        return text;
    }
}
