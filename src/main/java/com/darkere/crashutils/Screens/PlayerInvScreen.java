package com.darkere.crashutils.Screens;

import com.darkere.crashutils.CrashUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.awt.*;

public class PlayerInvScreen extends AbstractContainerScreen<PlayerInvContainer> {
    Rectangle doubleinv = new Rectangle(7, 14, 213, 229);
    private static final Identifier DoubleInv = Identifier.fromNamespaceAndPath(CrashUtils.MODID, "textures/gui/doubleinv.png");
    private static final CUTexture texture = new CUTexture(DoubleInv,7,14,220,243,256,256);
    PlayerInvContainer container;

    int centerX;
    int centerY;

    public PlayerInvScreen(PlayerInvContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        this.container = screenContainer;
    }


    @Override
    protected void init() {
        super.init();
        centerX = width / 2;
        centerY = height / 2;
        titleLabelX = centerX - 50;
        titleLabelY = centerY - 150;
        inventoryLabelX = centerX - 50;
        inventoryLabelY = centerY + 50;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        GuiTools.blit(graphics,texture,centerX,centerY,DrawPosition.Centered);
        if(hoveredSlot != null && !hoveredSlot.getItem().isEmpty()){
            graphics.setTooltipForNextFrame(Minecraft.getInstance().font,hoveredSlot.getItem(),mouseX,mouseY);
        }
        titleLabelX = 25;
        titleLabelY = -25;
        inventoryLabelX = 25;
        inventoryLabelY = 93;
    }
}
