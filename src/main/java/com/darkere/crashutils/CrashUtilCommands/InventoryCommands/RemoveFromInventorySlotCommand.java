package com.darkere.crashutils.CrashUtilCommands.InventoryCommands;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.WorldUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class RemoveFromInventorySlotCommand {
    private static final List<String> invTypes = new ArrayList<>(Arrays.asList("inventory", "armor", "offhand"));
    private static SuggestionProvider<CommandSource> invtype;

    public static ArgumentBuilder<CommandSource, ?> register() {
        if (CrashUtils.curiosLoaded && CuriosApi.getSlotHelper() != null) {
            invTypes.addAll(CuriosApi.getSlotHelper().getSlotTypeIds());
        }

        invtype = (ctx, builder) -> ISuggestionProvider.suggest(invTypes.stream(), builder);

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

    private static int removeFromSlot(CommandContext<CommandSource> context, String name, String inventoryType, int slot) {
        AtomicReference<IFormattableTextComponent> text = new AtomicReference<>(new StringTextComponent(""));
        AtomicBoolean success = new AtomicBoolean(false);
        WorldUtils.applyToPlayer(name, context.getSource().getServer(), (player) -> {
            switch (inventoryType) {
                case "inventory": {
                    if(player.inventory.items.get(slot).isEmpty()) {
                        success.set(false);
                        return;
                    }
                    text.set(player.inventory.items.get(slot).getDisplayName().copy());
                    player.inventory.items.set(slot, ItemStack.EMPTY);
                    break;
                }
                case "armor": {
                    if(player.inventory.armor.get(slot).isEmpty()) {
                        success.set(false);
                        return;
                    }
                    text.set(player.inventory.armor.get(slot).getDisplayName().copy());
                    player.inventory.armor.set(slot, ItemStack.EMPTY);
                    break;
                }
                case "offhand": {
                    if(player.inventory.offhand.get(slot).isEmpty()) {
                        success.set(false);
                        return;
                    }
                    text.set(player.inventory.offhand.get(slot).getDisplayName().copy());
                    player.inventory.offhand.set(slot, ItemStack.EMPTY);
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
            context.getSource().sendSuccess(text.get().append(new StringTextComponent(" has been deleted from " + name + "'s InventorySlot")), true);
        } else {
            context.getSource().sendSuccess(new StringTextComponent("Failed to delete item from slot" + slot +  ", slot is empty?"), true);
        }

        return 1;
    }

}
