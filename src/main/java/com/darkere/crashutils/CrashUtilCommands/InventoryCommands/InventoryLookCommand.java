package com.darkere.crashutils.CrashUtilCommands.InventoryCommands;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.WorldUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

public class InventoryLookCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("read")
            .then(Commands.argument("name", StringArgumentType.string())
                .suggests(CommandUtils.PROFILEPROVIDER)
                .executes(ctx -> printInventory(ctx, StringArgumentType.getString(ctx, "name"))));
    }

    public static int printInventory(CommandContext<CommandSourceStack> context, String name) {
        WorldUtils.applyToPlayer(name, context.getSource().getServer(), (playerEntity) -> {
            context.getSource().sendSuccess(CommandUtils.coloredComponent("Offhand", ChatFormatting.DARK_AQUA), true);
            for (int i = 0; i < playerEntity.getInventory().offhand.size(); i++) {
                if (!playerEntity.getInventory().offhand.get(i).isEmpty()) {
                    CommandUtils.sendItemInventoryRemovalMessage(context.getSource(), name, playerEntity.getInventory().offhand.get(i), "offhand", i);
                }
            }
            context.getSource().sendSuccess(CommandUtils.coloredComponent("Armor", ChatFormatting.DARK_AQUA), true);
            for (int i = 0; i < playerEntity.getInventory().armor.size(); i++) {
                if (!playerEntity.getInventory().armor.get(i).isEmpty()) {
                    CommandUtils.sendItemInventoryRemovalMessage(context.getSource(), name, playerEntity.getInventory().armor.get(i), "armor", i);
                }
            }
            context.getSource().sendSuccess(CommandUtils.coloredComponent("Inventory", ChatFormatting.DARK_AQUA), true);
            for (int i = 0; i < playerEntity.getInventory().items.size(); i++) {
                if (!playerEntity.getInventory().items.get(i).isEmpty()) {
                    CommandUtils.sendItemInventoryRemovalMessage(context.getSource(), name, playerEntity.getInventory().items.get(i), "inventory", i);
                }
            }
            if (CrashUtils.curiosLoaded) {
                context.getSource().sendSuccess(CommandUtils.coloredComponent("Curios", ChatFormatting.DARK_AQUA), true);
                LazyOptional<ICuriosItemHandler> itemHandler = CuriosApi.getCuriosHelper().getCuriosHandler(playerEntity);
                ICuriosItemHandler handler = itemHandler.orElse(null);
                handler.getCurios().forEach((k, v) -> {
                    context.getSource().sendSuccess(CommandUtils.coloredComponent(k, ChatFormatting.DARK_AQUA), true);
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
