package com.darkere.crashutils.Screens;

import com.darkere.crashutils.CommandUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuiTools {
    public static boolean inArea(int x, int y, int posX, int posY, int posEX, int posEY) {
        return x > posX && x < posEX && y > posY && y < posEY;
    }

    public static void drawTextToolTip(GuiGraphics guiGraphics, String s, int x, int y) {
        List<Component> text = new ArrayList<>();
        text.add(CommandUtils.CreateTextComponent(s));

        guiGraphics.renderTooltip( Minecraft.getInstance().font, text, Optional.empty(), x, y);
    }
    public static void drawTextToolTip(GuiGraphics guiGraphics, List<String> s, int x, int y) {
        List<Component> text = new ArrayList<>();
        s.forEach(stri -> text.add(CommandUtils.CreateTextComponent(stri)));

        guiGraphics.renderTooltip(Minecraft.getInstance().font,text, Optional.empty(), x, y);
    }

}
