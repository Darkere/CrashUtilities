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
        MutableComponent text = new TextComponent(msg);
        Style style = Style.EMPTY;
        text = text.setStyle(style);
        text.withStyle(color);
        source.sendSuccess(text, true);
    }


    public static void sendCommandMessage(CommandSourceStack source, MutableComponent text, String command, boolean runDirectly) {

        Style style = text.getStyle();
        ClickEvent click = new ClickEvent(runDirectly ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND, command);
        style = style.applyTo(style.withClickEvent(click));

        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click to execute \u00A76" + command + "\u00A7r"));
        style = style.applyTo(style.withHoverEvent(hoverEvent));
        MutableComponent tex = text.setStyle(style);
        source.sendSuccess(tex, false);
        LOGGER.info(text.getString() + " " + command);
    }

    public static void sendTEMessage(CommandSourceStack source, WorldPos worldPos, boolean runDirectly) {
        BlockPos pos = worldPos.pos;
        String position = " - " + "[" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "]";
        MutableComponent text = new TextComponent(position).withStyle(ChatFormatting.GREEN);
        ServerPlayer player = null;
        try {
            player = source.getPlayerOrException();
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        sendCommandMessage(source, text, "/cu tp " + (player != null ? player.getName().getString() : "Console") + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " + worldPos.type.location(), runDirectly);
    }

    public static void sendFindTEMessage(CommandSourceStack source, ResourceLocation res, int count, boolean ticking) {
        MutableComponent text = new TextComponent(res.toString()).withStyle(ChatFormatting.AQUA);
        text.append(new TextComponent(" Count ").withStyle(ChatFormatting.DARK_RED));
        text.append(new TextComponent(Integer.toString(count)).withStyle(ChatFormatting.GREEN));
        if (ticking)
            text.append(new TextComponent(" ticking").withStyle(ChatFormatting.RED));
        sendCommandMessage(source, text, "/cu tileentities find " + res.toString(), true);

    }

    public static void sendChunkEntityMessage(CommandSourceStack source, int count, BlockPos pos, ResourceKey<Level> type, boolean runDirectly) {
        MutableComponent text = new TextComponent("- " + pos.toString()).withStyle(ChatFormatting.GREEN);
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
        MutableComponent text = new TextComponent(String.valueOf(count)).withStyle(ChatFormatting.BLUE);
        text.append(new TextComponent("x ").withStyle(ChatFormatting.YELLOW));
        text.append(new TextComponent(res.toString()).withStyle(ChatFormatting.AQUA));
        sendCommandMessage(source, text, "/cu entities find " + res.toString(), true);

    }

    public static MutableComponent coloredComponent(String text, ChatFormatting color) {
        return new TextComponent(text).withStyle(color);
    }

    public static void sendItemInventoryRemovalMessage(CommandSourceStack source, String name, ItemStack itemStack, String inventoryType, int i) {
        MutableComponent text = new TextComponent("[" + i + "] ").withStyle(ChatFormatting.DARK_BLUE);
        text.append(itemStack.getDisplayName());
        String Command = "/cu inventory remove " + name + " " + inventoryType + " " + i;
        sendCommandMessage(source, text, Command, false);
    }

    public static MutableComponent createURLComponent(String display, String url) {
        MutableComponent text = new TextComponent(display);
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Go to " + url));
        Style style = Style.EMPTY;
        style = style.applyTo(style.withClickEvent(clickEvent));
        style = style.applyTo(style.withHoverEvent(hoverEvent));
        text.setStyle(style);
        return text;
    }

    public static MutableComponent createCopyComponent(String display, String toCopy) {
        MutableComponent text = new TextComponent(display);
        Style style = Style.EMPTY;
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, toCopy);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Copy Contents to Clipboard"));
        style = style.applyTo(style.withClickEvent(clickEvent));
        style = style.applyTo(style.withHoverEvent(hoverEvent));
        text.setStyle(style);
        text.withStyle(ChatFormatting.GREEN);
        return text;
    }

    public static MutableComponent getCommandTextComponent(String display, String command) {
        MutableComponent text = new TextComponent(display);
        Style style = Style.EMPTY;
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click to execute \u00A76" + command + "\u00A7r"));
        style = style.applyTo(style.withClickEvent(clickEvent));
        style = style.applyTo(style.withHoverEvent(hoverEvent));
        text.setStyle(style);
        text.withStyle(ChatFormatting.GOLD);
        return text;
    }
}
