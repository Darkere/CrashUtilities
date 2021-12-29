package com.darkere.crashutils.CrashUtilCommands.TileEntityCommands;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class TileEntitiesCommands {


    public static CommandNode<CommandSourceStack> register() {
        return Commands.literal("blockentity")
            .then(AllLoadedTEsCommand.register())
            .then(FindLoadedTEsCommand.register())
            .then(DeleteTECommand.register())
            .build();
    }

}
