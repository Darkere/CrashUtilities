package com.darkere.crashutils.CrashUtilCommands.EntityCommands;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class EntitiesCommands {


    public static CommandNode<CommandSource> register() {
       return Commands.literal("entities")
            .then(AllEntitiesCommand.register())
            .then(FindEntitiesCommand.register())
            .then(RemoveEntitiesCommand.register())
            .build();
    }

}
