package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.WorldUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

public class InventoryLookCommand {
    private static final SuggestionProvider<CommandSource> sugg = (ctx, builder) -> ISuggestionProvider.suggest(ctx.getSource().getServer().getPlayerProfileCache().gameProfiles.stream().map(GameProfile::getName), builder);

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("readInventory")
            .then(Commands.argument("name", StringArgumentType.string())
                .suggests(sugg)
                .executes(ctx -> printInventory(ctx, StringArgumentType.getString(ctx, "name"))));
    }

    public static int printInventory(CommandContext<CommandSource> context, String name) {
        WorldUtils.applyToPlayer(name, context, (playerEntity) -> {
            context.getSource().sendFeedback(CommandUtils.coloredComponent("Offhand", Color.func_240744_a_(TextFormatting.DARK_AQUA)), true);
            for (int i = 0; i < playerEntity.inventory.offHandInventory.size(); i++) {
                if (!playerEntity.inventory.offHandInventory.get(i).isEmpty()) {
                    CommandUtils.sendItemInventoryRemovalMessage(context.getSource(), name, playerEntity.inventory.offHandInventory.get(i), "offhand", i);
                }
            }
            context.getSource().sendFeedback(CommandUtils.coloredComponent("Armor", Color.func_240744_a_( TextFormatting.DARK_AQUA)), true);
            for (int i = 0; i < playerEntity.inventory.armorInventory.size(); i++) {
                if (!playerEntity.inventory.armorInventory.get(i).isEmpty()) {
                    CommandUtils.sendItemInventoryRemovalMessage(context.getSource(), name, playerEntity.inventory.armorInventory.get(i), "armor", i);
                }
            }
            context.getSource().sendFeedback(CommandUtils.coloredComponent("Inventory", Color.func_240744_a_( TextFormatting.DARK_AQUA)), true);
            for (int i = 0; i < playerEntity.inventory.mainInventory.size(); i++) {
                if (!playerEntity.inventory.mainInventory.get(i).isEmpty()) {
                    CommandUtils.sendItemInventoryRemovalMessage(context.getSource(), name, playerEntity.inventory.mainInventory.get(i), "inventory", i);
                }
            }
            if (CrashUtils.curiosLoaded) {
                context.getSource().sendFeedback(CommandUtils.coloredComponent("Curios", Color.func_240744_a_( TextFormatting.DARK_AQUA)), true);
                LazyOptional<ICuriosItemHandler> itemHandler = CuriosApi.getCuriosHelper().getCuriosHandler(playerEntity);
                ICuriosItemHandler handler = itemHandler.orElse(null);
                handler.getCurios().forEach((k, v) -> {
                    context.getSource().sendFeedback(CommandUtils.coloredComponent(k, Color.func_240744_a_( TextFormatting.DARK_AQUA)), true);
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
