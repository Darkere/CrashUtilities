package com.darkere.crashutils.CrashUtilCommands.TileEntityCommands;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class TileEntitiesCommands {


    public static CommandNode<CommandSource> register() {
        return Commands.literal("blockentity")
            .then(AllLoadedTEsCommand.register())
            .then(FindLoadedTEsCommand.register())
            .then(DeleteTECommand.register())
            .build();
    }

}
