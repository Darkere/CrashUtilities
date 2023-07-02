package com.darkere.crashutils.Screens;


import com.darkere.crashutils.CrashUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public enum CUTab {
    ITL(0, 0, 27, 26),
    ITC(28, 0, 27, 26),
    ITR(56, 0, 27, 26),
    ATL(0, 26, 27, 26),
    ATC(28, 26, 27, 26),
    ATR(56, 26, 27, 26),
    INVSEETABICON(0, 52, 63, 115),
    LISTTABICON(64, 52, 127, 115),
    MAPTABICON(128, 52, 191, 115);
    private static final ResourceLocation TABS = new ResourceLocation(CrashUtils.MODID, "textures/gui/tabs.png");
    int x, y, cx, cy;

    CUTab(int x, int y, int cx, int cy) {
        this.x = x;
        this.y = y;
        this.cx = cx;
        this.cy = cy;
    }

    public void drawTab(GuiGraphics gui, int i, int j, CUTab icon, float iconScale) {
        gui.blit(TABS, i, j, x, y, cx, cy);
        gui.blit(TABS,i + 5, j + 6, 2, (float) icon.x / iconScale, (float) icon.y / iconScale, 17, 17, (int) (256f / iconScale), (int) (256f / iconScale));
    }
}
