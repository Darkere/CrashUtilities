package com.darkere.crashutils.CrashUtilCommands.InventoryCommands;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.WorldUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class RemoveFromInventorySlotCommand {
    private static final List<String> invTypes = new ArrayList<>(Arrays.asList("inventory", "armor", "offhand"));
    private static SuggestionProvider<CommandSourceStack> invtype;

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        if (CrashUtils.curiosLoaded && CuriosApi.getSlotHelper() != null) {
            invTypes.addAll(CuriosApi.getSlotHelper().getSlotTypeIds());
        }

        invtype = (ctx, builder) -> SharedSuggestionProvider.suggest(invTypes.stream(), builder);

        return Commands.literal("remove")
            .then(Commands.argument("name", StringArgumentType.string())
                .suggests(CommandUtils.PROFILEPROVIDER)
                .then(Commands.argument("slotType", StringArgumentType.string())
                    .suggests(invtype)
                    .then(Commands.argument("slot", IntegerArgumentType.integer())
                        .executes(ctx -> removeFromSlot(ctx, StringArgumentType.getString(ctx, "name"), StringArgumentType.getString(ctx, "slotType"), IntegerArgumentType.getInteger(ctx, "slot")))))
                .then(Commands.argument("slot", IntegerArgumentType.integer())
                    .executes(ctx -> removeFromSlot(ctx, StringArgumentType.getString(ctx, "name"), "inventory", IntegerArgumentType.getInteger(ctx, "slot")))));
    }

    private static int removeFromSlot(CommandContext<CommandSourceStack> context, String name, String inventoryType, int slot) {
        AtomicReference<MutableComponent> text = new AtomicReference<>(CommandUtils.CreateTextComponent(""));
        AtomicBoolean success = new AtomicBoolean(false);
        WorldUtils.applyToPlayer(name, context.getSource().getServer(), (player) -> {
            switch (inventoryType) {
                case "inventory": {
                    if(player.getInventory().items.get(slot).isEmpty()) {
                        success.set(false);
                        return;
                    }
                    text.set(player.getInventory().items.get(slot).getDisplayName().copy());
                    player.getInventory().items.set(slot, ItemStack.EMPTY);
                    break;
                }
                case "armor": {
                    if(player.getInventory().armor.get(slot).isEmpty()) {
                        success.set(false);
                        return;
                    }
                    text.set(player.getInventory().armor.get(slot).getDisplayName().copy());
                    player.getInventory().armor.set(slot, ItemStack.EMPTY);
                    break;
                }
                case "offhand": {
                    if(player.getInventory().offhand.get(slot).isEmpty()) {
                        success.set(false);
                        return;
                    }
                    text.set(player.getInventory().offhand.get(slot).getDisplayName().copy());
                    player.getInventory().offhand.set(slot, ItemStack.EMPTY);
                    break;
                }
                default: {
                    if (CrashUtils.curiosLoaded && CuriosApi.getSlotHelper().getSlotTypeIds().contains(inventoryType)) {
                        ICuriosItemHandler handler = CuriosApi.getCuriosHelper().getCuriosHandler(player).orElse(null);
                        if(handler.getStacksHandler(inventoryType).get().getStacks().getStackInSlot(slot).isEmpty()) {
                            success.set(false);
                            return;
                        }
                        text.set(handler.getStacksHandler(inventoryType).get().getStacks().getStackInSlot(slot).getDisplayName().copy());
                        handler.getStacksHandler(inventoryType).get().getStacks().setStackInSlot(slot, ItemStack.EMPTY);
                    }
                }
            }
            success.set(true);
        });
        if(success.get()){
            context.getSource().sendSuccess(text.get().append(CommandUtils.CreateTextComponent(" has been deleted from " + name + "'s InventorySlot")), true);
        } else {
            context.getSource().sendSuccess(CommandUtils.CreateTextComponent("Failed to delete item from slot" + slot +  ", slot is empty?"), true);
        }

        return 1;
    }

}
