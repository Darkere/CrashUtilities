package com.darkere.crashutils.CrashUtilCommands.PlayerCommands;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.DataStructures.PlayerActivityHistory;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.world.level.Level;

import java.util.Arrays;

public class ActivityCommand {
    private static final SuggestionProvider<CommandSourceStack> sugg = (ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.asList("week", "day", "month"), builder);

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("activity")
            .executes(ActivityCommand::listActivity)
            .then(Commands.argument("time", StringArgumentType.word())
                .suggests(sugg)
                .executes(ctx -> listActivityByDate(ctx, StringArgumentType.getString(ctx, "time"))));
    }

    private static int listActivity(CommandContext<CommandSourceStack> context) {
        PlayerActivityHistory history = new PlayerActivityHistory(context.getSource().getLevel());
        context.getSource().sendSuccess(()->CommandUtils.CreateTextComponent("Number of Players active in the last:"), false);
        context.getSource().sendSuccess(()->CommandUtils.CreateTextComponent("Day: " + history.getDay().size() + " unique players"), false);
        context.getSource().sendSuccess(()->CommandUtils.CreateTextComponent("Week: " + history.getWeek().size() + " unique players"), false);
        context.getSource().sendSuccess(()->CommandUtils.CreateTextComponent("Month: " + history.getMonth().size() + " unique players"), false);
        return 1;
    }

    private static int listActivityByDate(CommandContext<CommandSourceStack> context, String time) {
        PlayerActivityHistory history = new PlayerActivityHistory(context.getSource().getServer().getLevel(Level.OVERWORLD));
        context.getSource().sendSuccess(()->CommandUtils.CreateTextComponent("Active Players in the last " + time), false);
        StringBuilder b = new StringBuilder();
        switch (time) {
            case "month":
                history.getMonth().forEach(x -> {
                    b.append(x);
                    b.append(", ");
                });
                break;
            case "week":
                history.getWeek().forEach(x -> {
                    b.append(x);
                    b.append(", ");
                });
                break;
            case "day":
                history.getDay().forEach(x -> {
                    b.append(x);
                    b.append(", ");
                });
                break;
        }
        context.getSource().sendSuccess(()->CommandUtils.CreateTextComponent(b.toString()), false);
        return 1;
    }


}
