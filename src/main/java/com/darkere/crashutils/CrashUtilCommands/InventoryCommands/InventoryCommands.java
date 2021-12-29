package com.darkere.crashutils.CrashUtilCommands.InventoryCommands;

import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class InventoryCommands {


    public static CommandNode<CommandSourceStack> register() {
        return Commands.literal("i")
            .then(InventoryLookCommand.register())
            .then(RemoveFromInventorySlotCommand.register())
            .then(InventoryOpenCommand.register())
            .then(InventoryOpenEnderChestCommand.register())
            .build();
    }

}
