package com.darkere.crashutils;

import com.darkere.crashutils.DataStructures.WorldPos;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandUtils {
    public static final int PERMISSION_LEVEL = 2;
    private static final Logger LOGGER = LogManager.getLogger();
    public static final SuggestionProvider<CommandSourceStack> PROFILEPROVIDER = (ctx, builder) ->
        SharedSuggestionProvider.suggest(ctx.getSource().getServer().getProfileCache().getTopMRUProfiles(1000).map(e -> e.getProfile().getName()), builder);

    public static void sendNormalMessage(CommandSourceStack source, String msg, ChatFormatting color) {
        MutableComponent text = CommandUtils.CreateTextComponent(msg);
        Style style = Style.EMPTY;
        text = text.setStyle(style);
        text.withStyle(color);
        source.sendSuccess(text, true);
    }


    public static void sendCommandMessage(CommandSourceStack source, MutableComponent text, String command, boolean runDirectly) {

        Style style = text.getStyle();
        ClickEvent click = new ClickEvent(runDirectly ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND, command);
        style = style.applyTo(style.withClickEvent(click));

        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, CommandUtils.CreateTextComponent("Click to execute \u00A76" + command + "\u00A7r"));
        style = style.applyTo(style.withHoverEvent(hoverEvent));
        MutableComponent tex = text.setStyle(style);
        source.sendSuccess(tex, false);
        LOGGER.info(text.getString() + " " + command);
    }

    public static void sendTEMessage(CommandSourceStack source, WorldPos worldPos, boolean runDirectly) {
        BlockPos pos = worldPos.pos;
        String position = " - " + "[" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "]";
        MutableComponent text = CommandUtils.CreateTextComponent(position).withStyle(ChatFormatting.GREEN);
        ServerPlayer player = null;
        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        sendCommandMessage(source, text, "/cu tp " + (player != null ? player.getName().getString() : "Console") + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " + worldPos.type.location(), runDirectly);
    }

    public static void sendFindTEMessage(CommandSourceStack source, ResourceLocation res, int count, boolean ticking) {
        MutableComponent text = CommandUtils.CreateTextComponent(res.toString()).withStyle(ChatFormatting.AQUA);
        text.append(CommandUtils.CreateTextComponent(" Count ").withStyle(ChatFormatting.DARK_RED));
        text.append(CommandUtils.CreateTextComponent(Integer.toString(count)).withStyle(ChatFormatting.GREEN));
        if (ticking)
            text.append(CommandUtils.CreateTextComponent(" ticking").withStyle(ChatFormatting.RED));
        sendCommandMessage(source, text, "/cu tileentities find " + res.toString(), true);

    }

    public static void sendChunkEntityMessage(CommandSourceStack source, int count, BlockPos pos, ResourceKey<Level> type, boolean runDirectly) {
        MutableComponent text = CommandUtils.CreateTextComponent("- " + pos.toString()).withStyle(ChatFormatting.GREEN);
        text.append(coloredComponent(" Count ", ChatFormatting.RED));
        text.append(coloredComponent(Integer.toString(count), ChatFormatting.GREEN));
        ServerPlayer player = null;
        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {

        }
        sendCommandMessage(source, text, "/cu tp " + (player != null ? player.getName().getString() : "Console") + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " + type.location(), runDirectly);
    }

    public static void sendFindEMessage(CommandSourceStack source, ResourceLocation res, int count) {
        MutableComponent text = CommandUtils.CreateTextComponent(String.valueOf(count)).withStyle(ChatFormatting.BLUE);
        text.append(CommandUtils.CreateTextComponent("x ").withStyle(ChatFormatting.YELLOW));
        text.append(CommandUtils.CreateTextComponent(res.toString()).withStyle(ChatFormatting.AQUA));
        sendCommandMessage(source, text, "/cu entities find " + res.toString(), true);

    }

    public static MutableComponent coloredComponent(String text, ChatFormatting color) {
        return CommandUtils.CreateTextComponent(text).withStyle(color);
    }

    public static void sendItemInventoryRemovalMessage(CommandSourceStack source, String name, ItemStack itemStack, String inventoryType, int i) {
        MutableComponent text = CommandUtils.CreateTextComponent("[" + i + "] ").withStyle(ChatFormatting.DARK_BLUE);
        text.append(itemStack.getDisplayName());
        String Command = "/cu inventory remove " + name + " " + inventoryType + " " + i;
        sendCommandMessage(source, text, Command, false);
    }

    public static MutableComponent createURLComponent(String display, String url) {
        MutableComponent text = CommandUtils.CreateTextComponent(display);
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, CommandUtils.CreateTextComponent("Go to " + url));
        Style style = Style.EMPTY;
        style = style.applyTo(style.withClickEvent(clickEvent));
        style = style.applyTo(style.withHoverEvent(hoverEvent));
        text.setStyle(style);
        return text;
    }

    public static MutableComponent createCopyComponent(String display, String toCopy) {
        MutableComponent text = CommandUtils.CreateTextComponent(display);
        Style style = Style.EMPTY;
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, toCopy);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, CommandUtils.CreateTextComponent("Copy Contents to Clipboard"));
        style = style.applyTo(style.withClickEvent(clickEvent));
        style = style.applyTo(style.withHoverEvent(hoverEvent));
        text.setStyle(style);
        text.withStyle(ChatFormatting.GREEN);
        return text;
    }

    public static MutableComponent getCommandTextComponent(String display, String command) {
        MutableComponent text = CommandUtils.CreateTextComponent(display);
        Style style = Style.EMPTY;
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, CommandUtils.CreateTextComponent("Click to execute \u00A76" + command + "\u00A7r"));
        style = style.applyTo(style.withClickEvent(clickEvent));
        style = style.applyTo(style.withHoverEvent(hoverEvent));
        text.setStyle(style);
        text.withStyle(ChatFormatting.GOLD);
        return text;
    }

    public static void sendMessageToPlayer(Player player, String text){
        player.sendSystemMessage(CommandUtils.CreateTextComponent(text));
    }
    public static MutableComponent CreateTextComponent(String text){
        return Component.literal(text);
    }
}
