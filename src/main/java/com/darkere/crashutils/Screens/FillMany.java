package com.darkere.crashutils.Screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

import java.util.List;

public class FillMany {
    public static final class ColoredRectangle {
        public final int x0;
        public final int y0;
        public final int x1;
        public final int y1;
        public final int color;

        public ColoredRectangle(int x0, int y0, int x1, int y1, int color) {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
            this.color = color;
        }
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

    public static void drawStrings(GuiGraphics guiGraphics, Font renderer, List<Text> texts) {
        for (Text text : texts) {
            guiGraphics.drawString(renderer,text.text,text.x,text.y,text.color, false);
//            renderer.drawInBatch(guiGraphics, text.text, text.x, text.y, text.color);
        }
    }

    public static void fillMany(Matrix4f matrix, List<ColoredRectangle> rects) {
//        RenderSystem.enableBlend();
//        RenderSystem.disableTexture();
//        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (ColoredRectangle rect : rects) {
            addRectangle(bufferbuilder, matrix, rect.x0, rect.y0, rect.x1, rect.y1, rect.color);
        }

        var result = bufferbuilder.build();
        if(result != null)
            BufferUploader.drawWithShader(result);

//        RenderSystem.enableTexture();
//        RenderSystem.disableBlend();
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
