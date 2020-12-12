package com.darkere.crashutils;

import com.darkere.crashutils.DataStructures.WorldPos;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandUtils {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final SuggestionProvider<CommandSource> PROFILEPROVIDER = (ctx, builder) ->
        ISuggestionProvider.suggest(ctx.getSource().getServer().getPlayerProfileCache().func_242117_a(1000).map(e -> e.getGameProfile().getName()), builder);

    public static void sendNormalMessage(CommandSource source, String msg, TextFormatting color) {
        IFormattableTextComponent text = new StringTextComponent(msg);
        Style style = Style.EMPTY;
        text = text.setStyle(style);
        text.mergeStyle(color);
        source.sendFeedback(text, true);
    }


    public static void sendCommandMessage(CommandSource source, IFormattableTextComponent text, String command, boolean runDirectly) {

        Style style = text.getStyle();
        ClickEvent click = new ClickEvent(runDirectly ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND, command);
        style = style.mergeStyle(style.setClickEvent(click));

        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click to execute \u00A76" + command + "\u00A7r"));
        style = style.mergeStyle(style.setHoverEvent(hoverEvent));
        IFormattableTextComponent tex = text.setStyle(style);
        source.sendFeedback(tex, false);
        LOGGER.info(text.getString() + " " + command);
    }

    public static void sendTEMessage(CommandSource source, WorldPos worldPos, boolean runDirectly) {
        BlockPos pos = worldPos.pos;
        String position = " - " + "[" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "]";
        IFormattableTextComponent text = new StringTextComponent(position).mergeStyle(TextFormatting.GREEN);
        ServerPlayerEntity player = null;
        try {
            player = source.asPlayer();
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        sendCommandMessage(source, text, "/cu player tp " + (player != null ? player.getName().getString() : "Console") + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " + worldPos.type.getLocation(), runDirectly);
    }

    public static void sendFindTEMessage(CommandSource source, ResourceLocation res, int count, boolean ticking) {
        IFormattableTextComponent text = new StringTextComponent(res.toString()).mergeStyle(TextFormatting.AQUA);
        text.append(new StringTextComponent(" Count ").mergeStyle(TextFormatting.DARK_RED));
        text.append(new StringTextComponent(Integer.toString(count)).mergeStyle(TextFormatting.GREEN));
        if (ticking)
            text.append(new StringTextComponent(" ticking").mergeStyle(TextFormatting.RED));
        sendCommandMessage(source, text, "/cu tileentities find " + res.toString(), true);

    }

    public static void sendChunkEntityMessage(CommandSource source, int count, BlockPos pos, RegistryKey<World> type, boolean runDirectly) {
        IFormattableTextComponent text = new StringTextComponent("- " + pos.toString()).mergeStyle(TextFormatting.GREEN);
        text.append(coloredComponent(" Count ", TextFormatting.RED));
        text.append(coloredComponent(Integer.toString(count), TextFormatting.GREEN));
        ServerPlayerEntity player = null;
        try {
            player = source.asPlayer();
        } catch (CommandSyntaxException e) {

        }
        sendCommandMessage(source, text, "/cu player tp " + (player != null ? player.getName().getString() : "Console") + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " + type.getLocation(), runDirectly);
    }

    public static void sendFindEMessage(CommandSource source, ResourceLocation res, int count) {
        IFormattableTextComponent text = new StringTextComponent(String.valueOf(count)).mergeStyle(TextFormatting.BLUE);
        text.append(new StringTextComponent("x ").mergeStyle(TextFormatting.YELLOW));
        text.append(new StringTextComponent(res.toString()).mergeStyle(TextFormatting.AQUA));
        sendCommandMessage(source, text, "/cu entities find " + res.toString(), true);

    }

    public static IFormattableTextComponent coloredComponent(String text, TextFormatting color) {
        return new StringTextComponent(text).mergeStyle(color);
    }

    public static void sendItemInventoryRemovalMessage(CommandSource source, String name, ItemStack itemStack, String inventoryType, int i) {
        IFormattableTextComponent text = new StringTextComponent("[" + i + "] ").mergeStyle(TextFormatting.DARK_BLUE);
        text.append(itemStack.getTextComponent());
        String Command = "/cu inventory remove " + name + " " + inventoryType + " " + i;
        sendCommandMessage(source, text, Command, false);
    }

    public static IFormattableTextComponent createURLComponent(String display, String url) {
        IFormattableTextComponent text = new StringTextComponent(display);
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Go to " + url));
        Style style = Style.EMPTY;
        style = style.mergeStyle(style.setClickEvent(clickEvent));
        style = style.mergeStyle(style.setHoverEvent(hoverEvent));
        text.setStyle(style);
        return text;
    }

    public static IFormattableTextComponent createCopyComponent(String display, String toCopy) {
        IFormattableTextComponent text = new StringTextComponent(display);
        Style style = Style.EMPTY;
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, toCopy);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Copy Contents to Clipboard"));
        style = style.mergeStyle(style.setClickEvent(clickEvent));
        style = style.mergeStyle(style.setHoverEvent(hoverEvent));
        text.setStyle(style);
        text.mergeStyle(TextFormatting.GREEN);
        return text;
    }

    public static IFormattableTextComponent getCommandTextComponent(String display, String command) {
        IFormattableTextComponent text = new StringTextComponent(display);
        Style style = Style.EMPTY;
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click to execute \u00A76" + command + "\u00A7r"));
        style = style.mergeStyle(style.setClickEvent(clickEvent));
        style = style.mergeStyle(style.setHoverEvent(hoverEvent));
        text.setStyle(style);
        text.mergeStyle(TextFormatting.GOLD);
        return text;
    }
}
