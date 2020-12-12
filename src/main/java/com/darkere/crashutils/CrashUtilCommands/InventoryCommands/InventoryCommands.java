package com.darkere.crashutils.CrashUtilCommands.InventoryCommands;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class InventoryCommands {


    public static CommandNode<CommandSource> register() {
        return Commands.literal("inventory")
            .then(InventoryLookCommand.register())
            .then(RemoveFromInventorySlotCommand.register())
            .build();
    }

}
