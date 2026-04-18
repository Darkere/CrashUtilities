package com.darkere.crashutils.Screens;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record MultiColoredRectangleRenderState(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2fc pose,
        List<FillMany.ColoredRectangle> Rectangles,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements GuiElementRenderState {
    public MultiColoredRectangleRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2fc pose,
            List<FillMany.ColoredRectangle> Rectangles,
            @Nullable ScreenRectangle scissorArea
    ) {
        this(pipeline, textureSetup, pose, Rectangles, scissorArea, getBounds(Rectangles, pose, scissorArea));
    }

    private static @Nullable ScreenRectangle getBounds(List<FillMany.ColoredRectangle> rectangles, Matrix3x2fc pose, @Nullable ScreenRectangle scissorArea) {
        int X = 0,Y = 0,W = 0,H = 0;
        for (FillMany.ColoredRectangle rectangle : rectangles) {
            X = Math.max(rectangle.x0(), X);
            Y = Math.max(rectangle.y0(), Y);
            W = Math.max(rectangle.x1(), W);
            H = Math.max(rectangle.y1(), H);
        }

        ScreenRectangle bounds = new ScreenRectangle(X,Y,W - X, H - Y).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer) {
        for (FillMany.ColoredRectangle rectangle : Rectangles) {
            vertexConsumer.addVertexWith2DPose(this.pose(), rectangle.x0(), rectangle.y0()).setColor(rectangle.color());
            vertexConsumer.addVertexWith2DPose(this.pose(), rectangle.x0(), rectangle.y1()).setColor(rectangle.color());
            vertexConsumer.addVertexWith2DPose(this.pose(), rectangle.x1(), rectangle.y1()).setColor(rectangle.color());
            vertexConsumer.addVertexWith2DPose(this.pose(), rectangle.x1(), rectangle.y0()).setColor(rectangle.color());
        }
    }
}
