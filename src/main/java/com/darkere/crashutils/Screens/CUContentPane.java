package com.darkere.crashutils.Screens;

import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.Network.DataRequestType;
import com.darkere.crashutils.Screens.Types.DropDownType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public abstract class CUContentPane {
    int defaultRenderOffsetX = -191;
    int defaultRenderOffsetY = -90;
    int centerX;
    int centerY;
    DataRequestType currentType = DataRequestType.LOADEDCHUNKDATA;
    boolean firstEntity = false;
    boolean firstTileEntity = false;
    boolean firstChunks = false;
    boolean firstPlayer = false;
    int updateSpeed = 5;
    boolean shouldUpdate = false;
    ResourceKey<Level> dim;
    CUScreen screen;
    int XTopLeft;
    int YTopLeft;
    int XAcross = 383;
    int YAcross = 190;

    public CUContentPane(ResourceKey<Level> dim, CUScreen screen) {
        this.dim = dim;
        this.screen = screen;
    }

    public void setUpdateSpeed() {
        if (shouldUpdate) {
            DataHolder.requestUpdates(updateSpeed * 1000, dim, false);
        } else {
            DataHolder.cancelTimer();
        }
    }

    public void addOffset(double x, double y) {

    }

    public void scroll(double x, double y, double delta, int centerX, int centerY) {
    }

    public boolean isMouseOver(double mx, double my, int centerX, int centerY) {
        return (mx >= XTopLeft && mx <= XTopLeft + XAcross && my >= YTopLeft && my <= YTopLeft + YAcross);
    }

    public void render(GuiGraphicsExtractor guiGraphics, int centerX, int centerY, int mx, int my, float partialTicks) {
        updateRenderValues(centerX, centerY);
        guiGraphics.fill( XTopLeft, YTopLeft, XAcross + XTopLeft, YAcross + YTopLeft, 0xFF000000);
    }

    public abstract void updateSelection(DropDownType ddtype, String s);

    public abstract boolean mouseClicked(MouseButtonEvent event, boolean doubleclick);

    public void updateRenderValues(int centerX, int centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
        XTopLeft = centerX + defaultRenderOffsetX;
        YTopLeft = centerY + defaultRenderOffsetY;
    }

    public abstract boolean mouseClickedOutside(MouseButtonEvent event, boolean doubleclick);

    public abstract boolean keyPressed(KeyEvent event);

    public abstract boolean charTyped(CharacterEvent event);

}
