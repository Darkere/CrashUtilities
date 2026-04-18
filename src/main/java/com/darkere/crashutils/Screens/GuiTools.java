package com.darkere.crashutils.Screens;

import com.darkere.crashutils.CommandUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GuiTools {
    public static boolean inArea(int x, int y, int posX, int posY, int posEX, int posEY) {
        return x > posX && x < posEX && y > posY && y < posEY;
    }

    public static void drawTextToolTip(GuiGraphicsExtractor guiGraphics, String s, int x, int y) {
        List<Component> text = new ArrayList<>();
        text.add(CommandUtils.CreateTextComponent(s));

        guiGraphics.setTooltipForNextFrame( Minecraft.getInstance().font, text, Optional.empty(), x, y);
    }
    public static void drawTextToolTip(GuiGraphicsExtractor guiGraphics, List<String> s, int x, int y) {
        List<Component> text = new ArrayList<>();
        s.forEach(stri -> text.add(CommandUtils.CreateTextComponent(stri)));

        guiGraphics.setTooltipForNextFrame(Minecraft.getInstance().font,text, Optional.empty(), x, y);
    }


    public static void blit(GuiGraphicsExtractor graphics, CUTexture texture, int LocationX, int LocationY, DrawPosition position)
    {
        blit(graphics, texture, LocationX, LocationY, texture.Width,texture.Height,position);
    }
    public static void blit(GuiGraphicsExtractor graphics, CUTexture texture, int LocationX, int LocationY, int SizeX, int SizeY, DrawPosition position)
    {
        if(position == DrawPosition.Centered)
        {
            LocationX -= SizeX/2;
            LocationY -= SizeY/2;
        }
        graphics.blit(texture.identifier,LocationX,LocationY,LocationX + SizeX, LocationY + SizeY,texture.u0,texture.u1,texture.v0,texture.v1);
    }

}
