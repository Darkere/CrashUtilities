package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.WorldUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
import java.util.concurrent.atomic.AtomicReference;

public class RemoveFromInventorySlotCommand {
    private static final List<String> invTypes = new ArrayList<>(Arrays.asList("inventory", "armor", "offhand"));
    private static final SuggestionProvider<CommandSource> sugg = (ctx, builder) -> ISuggestionProvider.suggest(ctx.getSource().getServer().getPlayerProfileCache().gameProfiles.stream().map(GameProfile::getName), builder);
    private static SuggestionProvider<CommandSource> invtype;

    public static ArgumentBuilder<CommandSource, ?> register() {
        if (CrashUtils.curiosLoaded && CuriosApi.getSlotHelper() != null) {
            invTypes.addAll(CuriosApi.getSlotHelper().getSlotTypeIds());


        }
        invtype = (ctx, builder) -> ISuggestionProvider.suggest(invTypes.stream(), builder);
        return Commands.literal("removeItemFromInventorySlot")
            .then(Commands.argument("name", StringArgumentType.string())
                .suggests(sugg)
                .then(Commands.argument("slotType", StringArgumentType.string())
                    .suggests(invtype)
                    .then(Commands.argument("slot", IntegerArgumentType.integer())
                        .executes(ctx -> removeFromSlot(ctx, StringArgumentType.getString(ctx, "name"), StringArgumentType.getString(ctx, "slotType"), IntegerArgumentType.getInteger(ctx, "slot"))))));
    }

    private static int removeFromSlot(CommandContext<CommandSource> context, String name, String inventoryType, int slot) throws CommandSyntaxException {
        AtomicReference<IFormattableTextComponent> text = new AtomicReference<>(new StringTextComponent(""));
        WorldUtils.applyToPlayer(name, context.getSource().getServer(), (player) -> {
            switch (inventoryType) {
                case "inventory": {
                    text.set(player.inventory.mainInventory.get(slot).getTextComponent().deepCopy());
                    player.inventory.mainInventory.set(slot, ItemStack.EMPTY);
                    break;
                }
                case "armor": {
                    text.set(player.inventory.armorInventory.get(slot).getTextComponent().deepCopy());
                    player.inventory.armorInventory.set(slot, ItemStack.EMPTY);
                    break;
                }
                case "offHand": {
                    text.set(player.inventory.offHandInventory.get(slot).getTextComponent().deepCopy());
                    player.inventory.offHandInventory.set(slot, ItemStack.EMPTY);
                    break;
                }
                default: {
                    if (CrashUtils.curiosLoaded && CuriosApi.getSlotHelper().getSlotTypeIds().contains(inventoryType)) {
                        ICuriosItemHandler handler = CuriosApi.getCuriosHelper().getCuriosHandler(player).orElse(null);
                        text.set(handler.getStacksHandler(inventoryType).get().getStacks().getStackInSlot(slot).getTextComponent().deepCopy());
                        handler.getStacksHandler(inventoryType).get().getStacks().setStackInSlot(slot, ItemStack.EMPTY);
                    }
                }
            }
        });
        context.getSource().sendFeedback(text.get().append(new StringTextComponent(" has been deleted from " + name + "'s InventorySlot")), true);

        return 1;
    }

}
