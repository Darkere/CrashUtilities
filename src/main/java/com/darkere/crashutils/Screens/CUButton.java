package com.darkere.crashutils.Screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

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
    //Copied from Extended Button
    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        int k = !this.active ? 0 : (this.isHoveredOrFocused() ? 2 : 1);
        guiGraphics.blitWithBorder(WIDGETS_LOCATION, this.getX(), this.getY(), 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2);

        Component buttonText = this.getMessage();

        guiGraphics.drawCenteredString(mc.font, buttonText, this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, getFGColor());
        if(isHovered())
            GuiTools.drawTextToolTip(guiGraphics,tooltips,mouseX,mouseY);
    }

    @Override
    public int getFGColor() {
       return this.isActive() ? 16777215 : 10526880; // White : Light Grey
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
    public boolean mouseClicked(double p_93641_, double p_93642_, int p_93643_) {
        if (this.isActive() && this.visible) {
            if (this.isValidClickButton(p_93643_)) {
                boolean flag = this.clicked(p_93641_, p_93642_);
                if (flag) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onClick(p_93641_, p_93642_);
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public interface OnTooltip {
        void onTooltip(Button p_93753_, PoseStack p_93754_, int p_93755_, int p_93756_);
    }
}
