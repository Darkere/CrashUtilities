package com.darkere.crashutils.Screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

import java.awt.*;
import java.util.List;

public class CUButton extends Button {
    List<String> tooltips;
    public CUButton(int xPos, int yPos, int width, int height, Component displayString, OnPress handler, String tooltip) {
        super(xPos, yPos, width, height, displayString, handler,DEFAULT_NARRATION);
        tooltips = List.of(tooltip);
    }
    public CUButton(int xPos, int yPos, int width, int height, Component displayString, OnPress handler, List<String> tooltip) {
        super(xPos, yPos, width, height, displayString, handler,DEFAULT_NARRATION);
        tooltips = tooltip;
    }

    @Override
    public int getFGColor() {
       return this.isActive() ? Color.WHITE.getRGB() : 10526880; // White : Light Grey
    }

//    @Override
//    protected int getYImage(boolean p_93668_) {
//        int i = 1;
//        if (!this.isActive()) {
//            i = 0;
//        } else if (p_93668_) {
//            i = 2;
//        }
//
//        return i;
//    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleclick) {
        if (this.isActive() && this.visible) {
            if (this.isValidClickButton(event.buttonInfo())) {
                boolean flag = this.isActive();
                if (flag) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onClick(event,doubleclick);
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        Minecraft mc = Minecraft.getInstance();
        int k = !this.active ? 0 : (this.isHoveredOrFocused() ? 2 : 1);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED,SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());
        //        guiGraphics.blitWithBorder(SPRITES.enabled(), this.getX(), this.getY(), 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2);

        Component buttonText = this.getMessage();
        graphics.centeredText(mc.font, buttonText, this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, getFGColor());
        if(isHovered())
            GuiTools.drawTextToolTip(graphics,tooltips,mouseX,mouseY);
    }
}
