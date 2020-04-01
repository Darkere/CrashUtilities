package com.darkere.crashutils;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandUtils {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void sendNormalMessage(CommandSource source, String msg, TextFormatting format) {
        StringTextComponent text = new StringTextComponent(msg);
        Style style = new Style();
        style.setColor(format);
        text.setStyle(style);
        source.sendFeedback(text, true);
    }


    public static void sendCommandMessage(CommandSource source, ITextComponent text, String command, boolean runDirectly) {

        Style style = text.getStyle();
        ClickEvent click = new ClickEvent(runDirectly ? ClickEvent.Action.RUN_COMMAND : ClickEvent.Action.SUGGEST_COMMAND, command);
        style.setClickEvent(click);

        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click to execute \u00A76" + command + "\u00A7r"));
        style.setHoverEvent(hoverEvent);

        source.sendFeedback(text.setStyle(style), false);
        LOGGER.info(text.getString() + " " + command);
    }

    public static void sendTEMessage(CommandSource source, TileEntity te, boolean runDirectly) {
        BlockPos pos = te.getPos();
        StringBuilder builder = new StringBuilder();
        builder.append(" - ");
        builder.append("[").append(pos.getX()).append(",").append(pos.getY()).append(",").append(pos.getZ()).append("]");
        ITextComponent text = new StringTextComponent(builder.toString()).setStyle(new Style().setColor(TextFormatting.GREEN));
        ServerPlayerEntity player = null;
        try {
            player = source.asPlayer();
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        sendCommandMessage(source, text, "/cu tp " + player.getName().getString() + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " + te.getWorld().getDimension().getType().getRegistryName(), runDirectly);
    }

    public static void sendFindTEMessage(CommandSource source, ResourceLocation res, int count, boolean ticking) {
        ITextComponent text = new StringTextComponent(res.toString()).setStyle(new Style().setColor(TextFormatting.AQUA));
        text.appendSibling(new StringTextComponent(" Count ").setStyle(new Style().setColor(TextFormatting.DARK_RED)));
        text.appendSibling(new StringTextComponent(Integer.toString(count)).setStyle(new Style().setColor(TextFormatting.GREEN)));
        if (ticking)
            text.appendSibling(new StringTextComponent(" ticking").setStyle(new Style().setColor(TextFormatting.RED)));
        sendCommandMessage(source, text, "/cu findLoadedTileEntities " + res.toString(), true);

    }

    public static void sendChunkEntityMessage(CommandSource source, int count, BlockPos pos, DimensionType type, boolean runDirectly) {
        ITextComponent text = new StringTextComponent("- " + pos.toString()).setStyle(new Style().setColor(TextFormatting.GREEN));
        text.appendSibling(coloredComponent(" Count ", TextFormatting.RED));
        text.appendSibling(coloredComponent(Integer.toString(count), TextFormatting.GREEN));
        ServerPlayerEntity player = null;
        try {
            player = source.asPlayer();
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
        }
        sendCommandMessage(source, text, "/cu tp " + player.getName().getString() + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " " + type.getRegistryName(), runDirectly);
    }

    public static void sendFindEMessage(CommandSource source, ResourceLocation res, int count) {
        ITextComponent text = new StringTextComponent(res.toString()).setStyle(new Style().setColor(TextFormatting.AQUA));
        text.appendSibling(new StringTextComponent(" Count: ").setStyle(new Style().setColor(TextFormatting.DARK_RED)));
        text.appendSibling(new StringTextComponent(count + " ").setStyle(new Style().setColor(TextFormatting.GREEN)));
        sendCommandMessage(source, text, "/cu findEntities " + res.toString(), true);

    }

    public static ITextComponent coloredComponent(String text, TextFormatting format) {
        return new StringTextComponent(text).setStyle(new Style().setColor(format));
    }

    public static void sendItemInventoryRemovalMessage(CommandSource source, String name, ItemStack itemStack, String inventoryType, int i) {
        ITextComponent text = new StringTextComponent("[" + i + "] ").setStyle(new Style().setColor(TextFormatting.DARK_BLUE));
        text.appendSibling(itemStack.getTextComponent());
        String Command = "/cu removeItemFromInventorySlot " + name + " " + inventoryType + " " + i;
        sendCommandMessage(source, text, Command, false);
    }
}
