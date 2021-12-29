package com.darkere.crashutils.Screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class GuiTools {
    public static boolean inArea(int x, int y, int posX, int posY, int posEX, int posEY) {
        return x > posX && x < posEX && y > posY && y < posEY;
    }

    public static void drawTextToolTip(PoseStack stack, String s, int x, int y, Screen screen) {
        List<TextComponent> text = new ArrayList<>();
        text.add(new TextComponent(s));
        //TODO: Fix Tooltips GuiUtils.drawHoveringText(stack, text, x, y, screen.width, screen.height, -1, Minecraft.getInstance().font);
    }
}
