package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.MemoryChecker;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class MemoryCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("memoryCheck")
            .executes(ctx -> run(ctx ,10))
            .then(Commands.argument("count",IntegerArgumentType.integer())
                .executes(ctx -> run(ctx ,IntegerArgumentType.getInteger(ctx,"count"))));
    }

    private static int run(CommandContext<CommandSource> context, int count){
        List<MemoryChecker.MemoryCount> full = CrashUtils.memoryChecker.counts;
        if(full.size() < count){
            count = full.size();
        }
        for (int i = full.size() - count; i < full.size() ; i++) { //last count elements
            context.getSource().sendFeedback(createVisualMemoryText(full.get(i)),true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static ITextComponent createVisualMemoryText(MemoryChecker.MemoryCount count){
        ITextComponent text = new StringTextComponent("[");
        double maximum =  (Math.ceil(MemoryChecker.inGigaBytes(count.getMaximum())));
        double total =   MemoryChecker.inGigaBytes(count.getTotal());
        double used = total -  MemoryChecker.inGigaBytes(count.getFree());
        double percentTotal = total /maximum;
        double percentUsed = used /maximum;

        for (double i = 0.1D; i<=1; i +=0.1D){
            if(i < percentUsed){
                text.appendSibling(CommandUtils.coloredComponent("I", TextFormatting.RED));
            } else if (i < percentTotal){
                text.appendSibling(CommandUtils.coloredComponent("I",TextFormatting.YELLOW));
            } else {
                text.appendSibling(CommandUtils.coloredComponent("I",TextFormatting.GREEN));
            }
        }
        int usedpercent = (int)(percentUsed * 100);
        int allocatedpercent = (int)(percentTotal* 100);
        text.appendSibling(CommandUtils.coloredComponent("] " + usedpercent + " % Used " + allocatedpercent + " % Allocated", TextFormatting.WHITE));

        return text;
    }
}
