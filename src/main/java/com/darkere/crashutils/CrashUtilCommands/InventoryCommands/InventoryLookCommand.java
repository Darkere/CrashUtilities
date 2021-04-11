package com.darkere.crashutils.CrashUtilCommands.InventoryCommands;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.WorldUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

public class InventoryLookCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("read")
            .then(Commands.argument("name", StringArgumentType.string())
                .suggests(CommandUtils.PROFILEPROVIDER)
                .executes(ctx -> printInventory(ctx, StringArgumentType.getString(ctx, "name"))));
    }

    public static int printInventory(CommandContext<CommandSource> context, String name) {
        WorldUtils.applyToPlayer(name, context.getSource().getServer(), (playerEntity) -> {
            context.getSource().sendSuccess(CommandUtils.coloredComponent("Offhand", TextFormatting.DARK_AQUA), true);
            for (int i = 0; i < playerEntity.inventory.offhand.size(); i++) {
                if (!playerEntity.inventory.offhand.get(i).isEmpty()) {
                    CommandUtils.sendItemInventoryRemovalMessage(context.getSource(), name, playerEntity.inventory.offhand.get(i), "offhand", i);
                }
            }
            context.getSource().sendSuccess(CommandUtils.coloredComponent("Armor", TextFormatting.DARK_AQUA), true);
            for (int i = 0; i < playerEntity.inventory.armor.size(); i++) {
                if (!playerEntity.inventory.armor.get(i).isEmpty()) {
                    CommandUtils.sendItemInventoryRemovalMessage(context.getSource(), name, playerEntity.inventory.armor.get(i), "armor", i);
                }
            }
            context.getSource().sendSuccess(CommandUtils.coloredComponent("Inventory", TextFormatting.DARK_AQUA), true);
            for (int i = 0; i < playerEntity.inventory.items.size(); i++) {
                if (!playerEntity.inventory.items.get(i).isEmpty()) {
                    CommandUtils.sendItemInventoryRemovalMessage(context.getSource(), name, playerEntity.inventory.items.get(i), "inventory", i);
                }
            }
            if (CrashUtils.curiosLoaded) {
                context.getSource().sendSuccess(CommandUtils.coloredComponent("Curios", TextFormatting.DARK_AQUA), true);
                LazyOptional<ICuriosItemHandler> itemHandler = CuriosApi.getCuriosHelper().getCuriosHandler(playerEntity);
                ICuriosItemHandler handler = itemHandler.orElse(null);
                handler.getCurios().forEach((k, v) -> {
                    context.getSource().sendSuccess(CommandUtils.coloredComponent(k, TextFormatting.DARK_AQUA), true);
                    for (int i = 0; i < v.getSlots(); i++) {
                        if (!v.getStacks().getStackInSlot(i).isEmpty()) {
                            CommandUtils.sendItemInventoryRemovalMessage(context.getSource(), name, v.getStacks().getStackInSlot(i), k, i);
                        }
                    }
                });
            }
        });
        return 1;
    }
}
