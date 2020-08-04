package com.darkere.crashutils;

import com.darkere.crashutils.DataStructures.WorldPos;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandUtils {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void sendNormalMessage(CommandSource source, String msg, Color color) {
        IFormattableTextComponent text = new StringTextComponent(msg);
        Style style = Style.EMPTY;
        style.setColor(color);
        text = text.func_240703_c_(style);
        source.sendFeedback(text, true);
    }


    public static void sendCommandMessage(CommandSource source, IFormattableTextComponent text, String command, boolean runDirectly) {

        Style style = text.getStyle();
        ClickEvent click = new ClickEvent(runDirectly ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND, command);
        style = style.mergeStyle(style.setClickEvent(click));

        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click to execute \u00A76" + command + "\u00A7r"));
        style = style.mergeStyle(style.setHoverEvent(hoverEvent));
        IFormattableTextComponent tex = text.func_240703_c_(style);
        source.sendFeedback(tex, false);
        LOGGER.info(text.getString() + " " + command);
    }

    public static void sendTEMessage(CommandSource source, WorldPos worldPos, boolean runDirectly) {
        BlockPos pos = worldPos.pos;
        String position = " - " + "[" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + "]";
        IFormattableTextComponent text = new StringTextComponent(position).func_240703_c_(Style.EMPTY.setColor(Color.func_240744_a_(TextFormatting.GREEN)));
        ServerPlayerEntity player = null;
        try {
            player = source.asPlayer();
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        sendCommandMessage(source, text, "/cu tp " + (player != null ? player.getName().getString() : "Console") + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " + worldPos.type.func_240901_a_(), runDirectly);
    }

    public static void sendFindTEMessage(CommandSource source, ResourceLocation res, int count, boolean ticking) {
        IFormattableTextComponent text = new StringTextComponent(res.toString()).func_240703_c_(Style.EMPTY.setColor(Color.func_240744_a_(TextFormatting.AQUA)));
        text.func_230529_a_(new StringTextComponent(" Count ").func_240703_c_(Style.EMPTY.setColor(Color.func_240744_a_(TextFormatting.DARK_RED))));
        text.func_230529_a_(new StringTextComponent(Integer.toString(count)).func_240703_c_(Style.EMPTY.setColor(Color.func_240744_a_(TextFormatting.GREEN))));
        if (ticking)
            text.func_230529_a_(new StringTextComponent(" ticking").func_240703_c_(Style.EMPTY.setColor(Color.func_240744_a_(TextFormatting.RED))));
        sendCommandMessage(source, text, "/cu findLoadedTileEntities " + res.toString(), true);

    }

    public static void sendChunkEntityMessage(CommandSource source, int count, BlockPos pos, RegistryKey<World> type, boolean runDirectly) {
        IFormattableTextComponent text = new StringTextComponent("- " + pos.toString()).func_240703_c_(Style.EMPTY.setColor(Color.func_240744_a_(TextFormatting.GREEN)));
        text.func_230529_a_(coloredComponent(" Count ", Color.func_240744_a_(TextFormatting.RED)));
        text.func_230529_a_(coloredComponent(Integer.toString(count), Color.func_240744_a_(TextFormatting.GREEN)));
        ServerPlayerEntity player = null;
        try {
            player = source.asPlayer();
        } catch (CommandSyntaxException e) {

        }
        sendCommandMessage(source, text, "/cu tp " + (player != null ? player.getName().getString() : "Console") + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " + type.func_240901_a_(), runDirectly);
    }

    public static void sendFindEMessage(CommandSource source, ResourceLocation res, int count) {
        IFormattableTextComponent text = new StringTextComponent(res.toString()).func_240703_c_(Style.EMPTY.setColor(Color.func_240744_a_(TextFormatting.AQUA)));
        text.func_230529_a_(new StringTextComponent(" Count: ").func_240703_c_(Style.EMPTY.setColor(Color.func_240744_a_(TextFormatting.DARK_RED))));
        text.func_230529_a_(new StringTextComponent(count + " ").func_240703_c_(Style.EMPTY.setColor(Color.func_240744_a_(TextFormatting.GREEN))));
        sendCommandMessage(source, text, "/cu findEntities " + res.toString(), true);

    }

    public static IFormattableTextComponent coloredComponent(String text, Color color) {
        return new StringTextComponent(text).func_240703_c_(Style.EMPTY.setColor(color));
    }

    public static void sendItemInventoryRemovalMessage(CommandSource source, String name, ItemStack itemStack, String inventoryType, int i) {
        IFormattableTextComponent text = new StringTextComponent("[" + i + "] ").func_240703_c_(Style.EMPTY.setColor(Color.func_240744_a_(TextFormatting.DARK_BLUE)));
        text.func_230529_a_(itemStack.getTextComponent());
        String Command = "/cu removeItemFromInventorySlot " + name + " " + inventoryType + " " + i;
        sendCommandMessage(source, text, Command, false);
    }

    public static IFormattableTextComponent createURLComponent(String display, String url) {
        IFormattableTextComponent text = new StringTextComponent(display);
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, url);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Go to " + url));
        Style style = Style.EMPTY;
        style = style.mergeStyle(style.setClickEvent(clickEvent));
        style = style.mergeStyle(style.setHoverEvent(hoverEvent));
        text.func_240703_c_(style);
        return text;
    }

    public static IFormattableTextComponent createCopyComponent(String display, String toCopy) {
        IFormattableTextComponent text = new StringTextComponent(display);
        Style style = Style.EMPTY;
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, toCopy);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Copy Contents to Clipboard"));
        style.setClickEvent(clickEvent);
        style.setHoverEvent(hoverEvent);
        style.setColor(Color.func_240744_a_(TextFormatting.GREEN));
        text.func_240703_c_(style);
        return text;
    }

    public static IFormattableTextComponent getCommandTextComponent(String display, String command) {
        IFormattableTextComponent text = new StringTextComponent(display);
        Style style = Style.EMPTY;
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click to execute \u00A76" + command + "\u00A7r"));
        style = style.mergeStyle(style.setClickEvent(clickEvent));
        style = style.mergeStyle(style.setHoverEvent(hoverEvent));
        style = style.mergeStyle(style.setColor(Color.func_240744_a_(TextFormatting.GOLD)));
        text.func_240703_c_(style);
        return text;
    }
}
