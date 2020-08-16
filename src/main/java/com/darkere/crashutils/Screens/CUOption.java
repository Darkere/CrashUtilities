package com.darkere.crashutils.Screens;

import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.DataStructures.DataHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CUOption {

    ResourceLocation rl;
    int number;
    ChunkPos chunkPos;
    BlockPos blockPos;
    String string;
    String extra;
    UUID id;

    public CUOption(String string, String extra) {
        this.string = string;
        this.extra = extra;
    }

    public CUOption(String string) {
        this.string = string;
    }

    public CUOption(String string, int number) {
        this.number = number;
        this.string = string;
    }

    public CUOption(ResourceLocation rl, int number) {
        this.rl = rl;
        this.number = number;
    }


    public CUOption(ChunkPos chunkPos, int number, ResourceLocation rl) {
        this.chunkPos = chunkPos;
        this.number = number;
        this.rl =rl;
    }

    public CUOption(ResourceLocation rl) {
        this.rl = rl;
    }

    public CUOption(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public CUOption(ChunkPos chunkPos) {
        this.chunkPos = chunkPos;
    }

    public CUOption(BlockPos pos, UUID id) {
        this.blockPos = pos;
        this.id = id;
    }

    public ResourceLocation getRl() {
        return rl;
    }

    public int getNumber() {
        return number;
    }

    public ChunkPos getChunkPos() {
        return chunkPos;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    List<Button> buttons = new ArrayList<>();

    public CUOption addButton(String text, Button.ITooltip tooltip, Button.IPressable action) {
        buttons.add(new Button(0, 0, Minecraft.getInstance().fontRenderer.getStringWidth(text) + 4, Minecraft.getInstance().fontRenderer.FONT_HEIGHT + 3, new StringTextComponent(text), action, tooltip));
        return this;
    }

    public int getButtonWidth(int x) {
        int width = 0;
        for (int i = buttons.size() - 1; i >= x; i--) {
            width += buttons.get(i).getWidth();
        }
        return width;
    }

    public List<Button> getButtons() {
        return buttons;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (number != 0) builder.append(number).append("x");
        if (rl != null) builder.append(" ").append(rl);
        if (chunkPos != null) builder.append(" ").append(chunkPos);
        if (blockPos != null) builder.append(" ").append(blockPos);
        if (string != null) builder.append(" ").append(string);
        if (extra != null) builder.append(" ").append(extra);
        return builder.toString();
    }

    public boolean shouldBeFilteredOut(String filter) {
        return rl != null && !rl.toString().startsWith(filter) && !rl.getPath().startsWith(filter);
    }

    public boolean checkClick(int mx, int my, CUScreen screen) {
        for (Button x : buttons) {
            if (x.isMouseOver(mx, my)) {
                x.onPress();
                CrashUtils.runInTwoTicks(() ->
                    DataHolder.requestImmediateUpdate(screen.dim)
                );

                return true;
            }
        }
        return false;
    }

    public String getString() {
        return string;
    }
}
