package com.darkere.crashutils.Screens;

import com.darkere.crashutils.CrashUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.awt.*;
import java.util.Map;

public class PlayerInvScreen extends ContainerScreen<PlayerInvContainer> {
    Rectangle doubleinv = new Rectangle(7, 14, 213, 229);
    Rectangle singleSlot = new Rectangle(221, 132, 28, 28);
    Rectangle leftSlot = new Rectangle(221, 132, 23, 28);
    Rectangle middleSlot = new Rectangle(226, 132, 18, 28);
    Rectangle rightSlot = new Rectangle(226, 132, 23, 28);
    private static final ResourceLocation texture = new ResourceLocation(CrashUtils.MODID, "textures/gui/doubleinv.png");
    PlayerInvContainer container;
    int curioRight;
    int curioLeft;
    int slotDistance;
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
        curioRight = 110;
        curioLeft = 110;

    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack p_230451_1_, int p_230451_2_, int p_230451_3_) {

    }

    @Override //drawGuiContainerBackgroundLayer
    protected void drawGuiContainerBackgroundLayer(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        renderTooltip(stack, new StringTextComponent(mouseX + " " + mouseY), mouseX, mouseY);
        Minecraft.getInstance().textureManager.bindTexture(texture);
        blit(stack, centerX - doubleinv.width / 2, centerY - doubleinv.height / 2, doubleinv.x, doubleinv.y, doubleinv.width, doubleinv.height);
        if (CrashUtils.curiosLoaded) {
            int y = 45;
            for (int g = 0; g < 2; g++) {
                int i = 0;
                for (Map.Entry<String, Integer> entry : container.slotAmounts.entrySet()) {
                    if (i == 4) {
                        y = g == 1 ? 165 : 45;
                    }
                    if (i < 4) {
                        drawSlotRangeRight(stack, entry.getValue(), centerX + curioRight, y);
                    } else {
                        drawSlotRangeLeft(stack, entry.getValue(), centerX - curioLeft, y);
                    }


                    i++;
                    y += 30;
                }
                y = 165;
            }

        }
    }

    private void drawSlotRangeRight(MatrixStack stack, int numberOfSlots, int x, int y) {
        if (numberOfSlots == 1) {
            blit(stack, x, y, singleSlot.x, singleSlot.y, singleSlot.width, singleSlot.height);
        } else {
            int dist = leftSlot.width + ((numberOfSlots - 2) * (middleSlot.width));
            blit(stack, x, y, leftSlot.x, leftSlot.y, leftSlot.width, leftSlot.height);
            blit(stack, x + dist, y, rightSlot.x, rightSlot.y, rightSlot.width, rightSlot.height);
            for (int i = 0; i < numberOfSlots - 2; i++) {
                int dist2 = leftSlot.width + i * (middleSlot.width);
                blit(stack, x + dist2, y, middleSlot.x, middleSlot.y, middleSlot.width, middleSlot.height);
            }
        }
    }

    private void drawSlotRangeLeft(MatrixStack stack, int numberOfSlots, int x, int y) {
        if (numberOfSlots == 1) {
            x -= singleSlot.width;
        } else {
            x = x - rightSlot.width - leftSlot.width - (numberOfSlots - 2) * middleSlot.width;
        }
        drawSlotRangeRight(stack, numberOfSlots, x, y);
    }
}
