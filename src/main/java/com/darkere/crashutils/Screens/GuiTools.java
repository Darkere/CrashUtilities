package com.darkere.crashutils.Screens;

import com.darkere.crashutils.CommandUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuiTools {
    public static boolean inArea(int x, int y, int posX, int posY, int posEX, int posEY) {
        return x > posX && x < posEX && y > posY && y < posEY;
    }

    public static void drawTextToolTip(PoseStack stack, String s, int x, int y, Screen screen) {
        List<Component> text = new ArrayList<>();
        text.add(CommandUtils.CreateTextComponent(s));

        screen.renderTooltip(stack, text, Optional.empty(), x, y, Minecraft.getInstance().font);
    }
    public static void drawTextToolTip(PoseStack stack, List<String> s, int x, int y, Screen screen) {
        List<Component> text = new ArrayList<>();
        s.forEach(stri -> text.add(CommandUtils.CreateTextComponent(stri)));

        screen.renderTooltip(stack, text, Optional.empty(), x, y, Minecraft.getInstance().font);
    }
}
