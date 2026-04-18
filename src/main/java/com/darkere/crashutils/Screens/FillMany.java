package com.darkere.crashutils.Screens;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.ColoredRectangleRenderState;
import org.joml.Matrix4f;

import java.util.List;

public class FillMany {
    public record ColoredRectangle(int x0, int y0, int x1, int y1, int color) {
    }

    public static final class Text {
        public final int x;
        public final int y;
        public final String text;
        int color;

        public Text(int x, int y, String text, int color) {
            this.x = x;
            this.y = y;
            this.text = text;
            this.color = color;
        }
    }

    public static void drawStrings(GuiGraphicsExtractor guiGraphics, Font renderer, List<Text> texts) {
        for (Text text : texts) {
            guiGraphics.text(renderer,text.text,text.x,text.y,text.color, false);
        }
    }

    public static void fillMany(GuiGraphicsExtractor graphics, List<ColoredRectangle> rects) {
        var matrix = graphics.pose().pushMatrix();
        graphics.submitGuiElementRenderState( new MultiColoredRectangleRenderState(
                RenderPipelines.GUI, TextureSetup.noTexture(), matrix,rects, graphics.peekScissorStack()));
        matrix.popMatrix();
    }

    private static void addRectangle(BufferBuilder bufferbuilder, Matrix4f matrix, int x0, int y0, int x1, int y1, int color) {
        if (x0 < x1) {
            int i = x0;
            x0 = x1;
            x1 = i;
        }
        if (y0 < y1) {
            int j = y0;
            y0 = y1;
            y1 = j;
        }
        float ca = (float) (color >> 24 & 255) / 255.0F;
        float cr = (float) (color >> 16 & 255) / 255.0F;
        float cg = (float) (color >> 8 & 255) / 255.0F;
        float cb = (float) (color & 255) / 255.0F;
        bufferbuilder.addVertex(matrix, (float) x0, (float) y1, 0.0F).setColor(cr, cg, cb, ca);
        bufferbuilder.addVertex(matrix, (float) x1, (float) y1, 0.0F).setColor(cr, cg, cb, ca);
        bufferbuilder.addVertex(matrix, (float) x1, (float) y0, 0.0F).setColor(cr, cg, cb, ca);
        bufferbuilder.addVertex(matrix, (float) x0, (float) y0, 0.0F).setColor(cr, cg, cb, ca);
    }
}
