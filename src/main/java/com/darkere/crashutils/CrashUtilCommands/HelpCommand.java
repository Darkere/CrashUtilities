package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.CommandUtils;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.Arrays;
import java.util.List;

public class HelpCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("help")
            .executes(HelpCommand::run);


    }

    private static int run(CommandContext<CommandSourceStack> context) {
        List<String> text = Arrays.asList(
            "/cu entities",
            "   list: Lists all loaded Entities on the Server",
            "   find (name): Finds all Entities of this type on the Server",
            "   remove: (<force> removes the entities immediately without waiting for their next tick) ",
            "       byType (name) <force>: removes all entities of that type",
            "       hostile <force>: removes all entities that are tagged as hostile",
            "       regex (regex) <force>: removes all entities whose registry key matches this regex",
            "/cu tileentities",
            "   list: list all loaded Tile Entities on the Server",
            "   find (name): list all loaded Tile Entities of this type",
            "   remove (pos): remove the Tile Entity at this position (does not remove block!)",
            "/cu inventory",
            "   read (name): reads out the Inventory of this player",
            "   remove (name) <slot type>(slot): removes the item from the slot of this player",
            "   open (name): opens the inventory of a player",
            "   enderchest (name): opens the enderchest of a player",
            "/cu activity: list playeractivity over the last days,week,month",
            "/cu tp (name)",
            "   (pos) (dim): teleport to position in dimension",
            "   (name): teleport to player",
            "/cu log: post links to copying or uploading logs",
            "/cu chunks: report all loaded chunks");

        for (String s : text) {
            context.getSource().sendSuccess(()->CommandUtils.CreateTextComponent(s), true);
        }
        return 1;
    }
}
