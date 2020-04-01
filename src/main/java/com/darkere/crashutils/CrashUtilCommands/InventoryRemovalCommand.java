package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.WorldUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ItemArgument;
import net.minecraft.command.arguments.ItemInput;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

public class InventoryRemovalCommand {
    private static final SuggestionProvider<CommandSource> sugg = (ctx, builder) -> ISuggestionProvider.suggest(ctx.getSource().getServer().getPlayerProfileCache().gameProfiles.stream().map(GameProfile::getName), builder);
    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("removeAllFromInventory")
            .then(Commands.argument("name", StringArgumentType.string())
                .suggests(sugg)
                .then(Commands.argument("item", ItemArgument.item())
                .executes(ctx -> removeAll(ctx,StringArgumentType.getString(ctx,"name"),ItemArgument.getItem(ctx,"item")))));
    }

    public static int removeAll(CommandContext<CommandSource> context, String playerName, ItemInput itemInput ) throws CommandSyntaxException {
        ItemStack stack = itemInput.createStack(1,false);
        WorldUtils.applyToPlayer(playerName,context,(player)->{
            player.inventory.clearMatchingItems((itemStack -> itemStack.getItem().equals(stack.getItem())),Integer.MAX_VALUE);
        });
        context.getSource().sendFeedback(stack.getTextComponent().appendSibling(new StringTextComponent(" has been removed from " + playerName+ "'s Inventory")),true);
        return 1;
    }
}
