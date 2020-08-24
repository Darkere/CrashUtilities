package com.darkere.crashutils.Screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;

public class GuiTools {
    public static boolean inArea(int x, int y, int posX, int posY, int posEX, int posEY) {
        return x > posX && x < posEX && y > posY && y < posEY;
    }

    public static void drawTextToolTip(MatrixStack stack, String s, int x, int y, Screen screen) {
        List<StringTextComponent> text = new ArrayList<>();
        text.add(new StringTextComponent(s));
       // GuiUtils.drawHoveringText(stack,text,x,y,screen.width,screen.height,-1, Minecraft.getInstance().fontRenderer);
    }
}
