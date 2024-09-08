package com.darkere.crashutils.Screens;

import com.darkere.crashutils.CrashUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.awt.*;

public class PlayerInvScreen extends AbstractContainerScreen<PlayerInvContainer> {
    Rectangle doubleinv = new Rectangle(7, 14, 213, 229);
    private static final ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(CrashUtils.MODID, "textures/gui/doubleinv.png");
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
    }


    @Override //drawGuiContainerBackgroundLayer
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        //renderTooltip(stack, new StringTextComponent(mouseX + " " + mouseY), mouseX, mouseY);
        guiGraphics.blit(texture, centerX - doubleinv.width / 2, centerY - doubleinv.height / 2, doubleinv.x, doubleinv.y, doubleinv.width, doubleinv.height);
        if(hoveredSlot != null && !hoveredSlot.getItem().isEmpty()){
            guiGraphics.renderTooltip(Minecraft.getInstance().font, hoveredSlot.getItem().getDisplayName(),mouseX,mouseY);
        }
    }
}
