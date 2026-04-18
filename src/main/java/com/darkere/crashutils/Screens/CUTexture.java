package com.darkere.crashutils.Screens;

import net.minecraft.resources.Identifier;

public class CUTexture {
    float u0,u1,v0,v1;
    Identifier identifier;
    int TextureLeft;
    int TextureTop;
    int TextureRight;
    int TextureBottom;
    int TotalTextureWidth;
    int TotalTextureHeight;
    int Width;
    int Height;
    public CUTexture(Identifier identifier, int textureLeft, int textureTop, int textureRight, int textureBottom, int totalTextureWidth, int totalTextureHeight) {
        this.identifier = identifier;
        TextureLeft = textureLeft;
        TextureTop = textureTop;
        TextureRight = textureRight;
        TextureBottom = textureBottom;
        TotalTextureWidth = totalTextureWidth;
        TotalTextureHeight = totalTextureHeight;
        u0 = (float) TextureLeft / totalTextureWidth;
        u1 = (float) TextureRight / totalTextureWidth;
        v0 = (float) TextureTop / totalTextureHeight;
        v1 = (float) TextureBottom / totalTextureHeight;
        Width = TextureRight - TextureLeft;
        Height = TextureBottom - TextureTop;
    }
}
