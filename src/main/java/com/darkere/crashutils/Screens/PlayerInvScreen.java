package com.darkere.crashutils.Screens;

import com.darkere.crashutils.CrashUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.awt.*;

public class PlayerInvScreen extends AbstractContainerScreen<PlayerInvContainer> {
    Rectangle doubleinv = new Rectangle(7, 14, 213, 229);
    private static final ResourceLocation texture = new ResourceLocation(CrashUtils.MODID, "textures/gui/doubleinv.png");
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

    @Override
    protected void renderLabels(PoseStack p_230451_1_, int p_230451_2_, int p_230451_3_) {

    }

    @Override //drawGuiContainerBackgroundLayer
    protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
        //renderTooltip(stack, new StringTextComponent(mouseX + " " + mouseY), mouseX, mouseY);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        blit(stack, centerX - doubleinv.width / 2, centerY - doubleinv.height / 2, doubleinv.x, doubleinv.y, doubleinv.width, doubleinv.height);
        if(hoveredSlot != null && !hoveredSlot.getItem().isEmpty()){
            renderTooltip(stack, hoveredSlot.getItem().getDisplayName(),mouseX,mouseY);
        }
    }
}
