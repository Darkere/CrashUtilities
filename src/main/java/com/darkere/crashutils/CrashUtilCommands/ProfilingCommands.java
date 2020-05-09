package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.DataStructures.ProfilingData;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.profiler.IProfileResult;


public class ProfilingCommands {

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("profile")
            .executes(ProfilingCommands::run);

    }

    private static int run(CommandContext<CommandSource> context) {
        IProfileResult result = context.getSource().getServer().getProfiler().getFixedProfiler().disable();
        new ProfilingData(result);


        return 1;
    }



}
