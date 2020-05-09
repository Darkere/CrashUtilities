package com.darkere.crashutils.Screens;


import net.minecraft.client.gui.AbstractGui;

public enum CUTab {
    ITL(0, 0, 27, 26),
    ITC(28, 0, 27, 26),
    ITR(56, 0, 27, 26),
    ATL(0, 26, 27, 26),
    ATC(28, 26, 27, 26),
    ATR(56, 26, 27, 26);

    int x, y, cx, cy;

    CUTab(int x, int y, int cx, int cy) {
        this.x = x;
        this.y = y;
        this.cx = cx;
        this.cy = cy;
    }
    public void drawTab(AbstractGui gui, int i, int j){
        gui.blit(i,j,x,y,cx,cy);
    }
}
