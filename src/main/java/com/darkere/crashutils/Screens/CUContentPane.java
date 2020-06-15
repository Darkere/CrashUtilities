package com.darkere.crashutils.Screens;

import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.Network.DataRequestType;
import com.darkere.crashutils.Screens.Types.DropDownType;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.world.dimension.DimensionType;

public abstract class CUContentPane extends AbstractGui {
    int defaultRenderOffsetX = -190;
    int defaultRenderOffsetY = -88;
    int centerX;
    int centerY;
    DataRequestType currentType = DataRequestType.LOADEDCHUNKDATA;
    boolean firstEntity = false;
    boolean firstTileEntity = false;
    boolean firstChunks = false;
    int updateSpeed = 60;
    boolean shouldUpdate = false;
    DimensionType dim;
    CUScreen screen;

    public CUContentPane(DimensionType dim, CUScreen screen) {
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
        return (mx >= centerX + defaultRenderOffsetX && mx <= centerX - defaultRenderOffsetX && my >= centerY + defaultRenderOffsetY && my <= centerY - defaultRenderOffsetY);
    }

    public void render(int centerX, int centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
    }

    public abstract void updateSelection(DropDownType ddtype, String s);
}
