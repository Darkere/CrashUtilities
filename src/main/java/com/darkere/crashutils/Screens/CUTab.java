package com.darkere.crashutils.Screens;


import com.darkere.crashutils.CrashUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public enum CUTab {
    ITL(0, 0, 27, 26),
    ITC(28, 0, 27, 26),
    ITR(56, 0, 27, 26),
    ATL(0, 26, 27, 26),
    ATC(28, 26, 27, 26),
    ATR(56, 26, 27, 26),
    INVSEETABICON(0, 52, 63, 63),
    LISTTABICON(64, 52, 63, 63),
    MAPTABICON(128, 52, 63, 63);
    private final Identifier TABS = Identifier.fromNamespaceAndPath(CrashUtils.MODID, "textures/gui/tabs.png");
    final int Left;
    final int Top;
    final int width;
    final int height;
    final CUTexture Texture;

    CUTab(int Left, int Top, int width, int height) {
        this.Left = Left;
        this.Top = Top;
        this.width = width;
        this.height = height;
        Texture = new CUTexture(TABS,Left,Top,Left + width,Top + height,256,256);
    }

    public void drawTab(GuiGraphicsExtractor gui, int i, int j, CUTab icon, float iconScale) {
        GuiTools.blit(gui, Texture, i, j, DrawPosition.Topleft);
        GuiTools.blit(gui, icon.Texture,i + Texture.Width /2, j + Texture.Height/2,16,16, DrawPosition.Centered);
//        gui.blit(RenderPipelines.GUI_TEXTURED,TABS,i + 5, j + 6, (float) icon.x / iconScale, (float) icon.y / iconScale, 17, 17, (int) (256f / iconScale), (int) (256f / iconScale));
    }
}
