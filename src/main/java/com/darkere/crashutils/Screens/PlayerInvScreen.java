package com.darkere.crashutils.Screens;

import com.darkere.crashutils.CrashUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.awt.*;

public class PlayerInvScreen extends ContainerScreen<PlayerInvContainer> {
    Rectangle doubleinv = new Rectangle(7, 14, 213, 229);
    private static final ResourceLocation texture = new ResourceLocation(CrashUtils.MODID, "textures/gui/doubleinv.png");
    PlayerInvContainer container;

    int centerX;
    int centerY;

    public PlayerInvScreen(PlayerInvContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
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
    protected void renderLabels(MatrixStack p_230451_1_, int p_230451_2_, int p_230451_3_) {

    }

    @Override //drawGuiContainerBackgroundLayer
    protected void renderBg(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        //renderTooltip(stack, new StringTextComponent(mouseX + " " + mouseY), mouseX, mouseY);
        Minecraft.getInstance().textureManager.bind(texture);
        blit(stack, centerX - doubleinv.width / 2, centerY - doubleinv.height / 2, doubleinv.x, doubleinv.y, doubleinv.width, doubleinv.height);
        if(hoveredSlot != null && !hoveredSlot.getItem().isEmpty()){
            renderTooltip(stack, hoveredSlot.getItem().getDisplayName(),mouseX,mouseY);
        }
    }
}
