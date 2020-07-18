package com.darkere.crashutils.Screens;

import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.Network.DataRequestType;
import com.darkere.crashutils.Screens.Types.DropDownType;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

public abstract class CUContentPane extends AbstractGui {
    int defaultRenderOffsetX = -191;
    int defaultRenderOffsetY = -90;
    int centerX;
    int centerY;
    DataRequestType currentType = DataRequestType.LOADEDCHUNKDATA;
    boolean firstEntity = false;
    boolean firstTileEntity = false;
    boolean firstChunks = false;
    int updateSpeed = 60;
    boolean shouldUpdate = false;
    RegistryKey<World> dim;
    CUScreen screen;
    int XTopLeft;
    int YTopLeft;
    int XAcross = 383;
    int YAcross = 190;

    public CUContentPane(RegistryKey<World> dim, CUScreen screen) {
        this.dim = dim;
        this.screen = screen;
    }

    public void setUpdateSpeed() {
        if (shouldUpdate) {
            DataHolder.requestUpdates(currentType, updateSpeed * 1000, dim, false);
        } else {
            DataHolder.cancelTimer();
        }
    }

    public void addOffset(double x, double y) {

    }

    public void zoom(double x, double y, double delta, int centerX, int centerY) {
    }

    public boolean isMouseOver(double mx, double my, int centerX, int centerY) {
        return (mx >= XTopLeft && mx <= XTopLeft + XAcross && my >= YTopLeft && my <= YTopLeft + YAcross);
    }

    public void render(MatrixStack stack, int centerX, int centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
        XTopLeft = centerX + defaultRenderOffsetX;
        YTopLeft = centerY + defaultRenderOffsetY;
        fill(stack, XTopLeft, YTopLeft, XAcross + XTopLeft, YAcross + YTopLeft, 0xFF000000);
    }

    public abstract void updateSelection(DropDownType ddtype, String s);
}
