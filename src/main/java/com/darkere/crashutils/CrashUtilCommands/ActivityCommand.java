package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.DataStructures.PlayerActivityHistory;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.StringTextComponent;

import java.util.Arrays;

public class ActivityCommand {
    private static final SuggestionProvider<CommandSource> sugg = (ctx, builder) -> ISuggestionProvider.suggest(Arrays.asList("week", "day", "month"), builder);

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("playerActivity")
            .executes(ActivityCommand::listActivity)
            .then(Commands.argument("time", StringArgumentType.word())
                .suggests(sugg)
                .executes(ctx -> listActivityByDate(ctx, StringArgumentType.getString(ctx, "time"))));
    }

    private static int listActivity(CommandContext<CommandSource> context) {
        PlayerActivityHistory history = new PlayerActivityHistory(context.getSource().getWorld());
        context.getSource().sendFeedback(new StringTextComponent("Number of Players active in the last:"), false);
        context.getSource().sendFeedback(new StringTextComponent("Day: " + history.getDay().size() + " unique players"), false);
        context.getSource().sendFeedback(new StringTextComponent("Week: " + history.getWeek().size() + " unique players"), false);
        context.getSource().sendFeedback(new StringTextComponent("Month: " + history.getMonth().size() + " unique players"), false);
        return 1;
    }

    private static int listActivityByDate(CommandContext<CommandSource> context, String time) {
        PlayerActivityHistory history = new PlayerActivityHistory(context.getSource().getWorld());
        context.getSource().sendFeedback(new StringTextComponent("Active Players in the last " + time), false);
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
        context.getSource().sendFeedback(new StringTextComponent(b.toString()), false);
        return 1;
    }


}
