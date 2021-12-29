package com.darkere.crashutils.CrashUtilCommands.EntityCommands;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class EntitiesCommands {


    public static CommandNode<CommandSourceStack> register() {
       return Commands.literal("e")
            .then(AllEntitiesCommand.register())
            .then(FindEntitiesCommand.register())
            .then(RemoveEntitiesCommand.register())
            .build();
    }

}
