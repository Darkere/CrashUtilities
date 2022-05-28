package com.darkere.crashutils.Screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.GuiUtils;

public class CUButton extends Button {
    public CUButton(int xPos, int yPos, int width, int height, Component displayString, OnPress handler, OnTooltip tooltip) {
        super(xPos, yPos, width, height, displayString, handler,tooltip);
    }

    //Copied from Extended Button
    @Override
    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        int k = this.getYImage(this.isHoveredOrFocused());
        GuiUtils.drawContinuousTexturedBox(poseStack, WIDGETS_LOCATION, this.x, this.y, 0, 46 + k * 20, this.width, this.height, 200, 20, 2, 3, 2, 2, this.getBlitOffset());
        this.renderBg(poseStack, mc, mouseX, mouseY);

        Component buttonText = this.getMessage();

        drawCenteredString(poseStack, mc.font, buttonText, this.x + this.width / 2, this.y + (this.height - 8) / 2, getFGColor());
    }

    @Override
    public int getFGColor() {
       return this.isActive() ? 16777215 : 10526880; // White : Light Grey
    }

    @Override
    protected int getYImage(boolean p_93668_) {
        int i = 1;
        if (!this.isActive()) {
            i = 0;
        } else if (p_93668_) {
            i = 2;
        }

        return i;
    }

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
}
