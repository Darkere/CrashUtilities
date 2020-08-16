package com.darkere.crashutils.Screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CUList extends AbstractGui {
    List<CUOption> allOptions;
    List<CUOption> currentOptions = new ArrayList<>();
    private final int posX;
    private final int posY;
    private final int width;
    private final int height;
    CUScreen parent;
    private int currentOffset = 0;
    private int maxOffset;
    private boolean isEnabled;
    private final int lineHeight = Minecraft.getInstance().fontRenderer.FONT_HEIGHT + 4;
    private final int fitOnScreen = 14;
    private Consumer<List<CUOption>> sorter;
    private Consumer<CUOption> action;

    public CUList(List<CUOption> allOptions, int posX, int posY, int width, int height, CUScreen parent, Consumer<List<CUOption>> sorter, Consumer<CUOption> action) {
        this.allOptions = allOptions;
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        this.parent = parent;
        this.isEnabled = true;
        this.sorter = sorter;
        updateFilter("");
        this.action = action;
    }

    public void render(MatrixStack stack, int mx, int my, float partialTicks) {
        if (!isEnabled) return;
        int x = posX;
        int y = posY + 1;
        List<FillMany.Text> text = new ArrayList<>();
        List<FillMany.ColoredRectangle> seps = new ArrayList<>();
        for (int j = currentOffset; j < currentOptions.size(); j++) {
            CUOption currentOption = currentOptions.get(j);
            text.add(new FillMany.Text(x, y, currentOption.toString(), -1));
            seps.add(new FillMany.ColoredRectangle(x, y + lineHeight - 3, x + width, y + lineHeight - 2, -1));
            List<Button> buttons = currentOption.getButtons();
            for (int i = 0, buttonsSize = buttons.size(); i < buttonsSize; i++) {
                Button button = buttons.get(i);
                button.x = x + width - currentOption.getButtonWidth(i);
                button.y = y - 1;
                button.isHovered = button.isMouseOver(mx, my);
                button.renderButton(stack, button.x, button.y, partialTicks);
            }
            y += lineHeight;
            if (j == currentOffset + fitOnScreen) break;
        }
        FillMany.drawStrings(stack, Minecraft.getInstance().fontRenderer, text);
        FillMany.fillMany(stack.getLast().getMatrix(), seps);

    }

    public void updateOptions(List<CUOption> options, Consumer<List<CUOption>> sorter, Consumer<CUOption> action) {
        allOptions = options;
        this.sorter = sorter;
        this.action = action;
        updateFilter("");
    }

    public void updateFilter(String filter) {
        currentOptions.clear();
        currentOptions.addAll(allOptions);
        currentOptions.removeIf(x -> x.shouldBeFilteredOut(filter));
        if (sorter != null) sorter.accept(currentOptions);
        recalculateMaxOffset();
    }

    private void recalculateMaxOffset() {
        maxOffset = Math.max(0, currentOptions.size() - fitOnScreen);
        if (currentOffset > maxOffset) currentOffset = maxOffset;
    }

    public boolean scroll(double mx, double my, double delta) {
        if (!isEnabled) return false;
        if (GuiTools.inArea((int) mx, (int) my, posX, posY, posX + width, posY + height)) {
            if (maxOffset > 0) {
                if (delta < 0) {
                    if (currentOffset < maxOffset) {
                        currentOffset++;
                    }
                } else {
                    if (currentOffset > 0) {
                        currentOffset--;
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean checkClick(int mx, int my) {
        if (!isEnabled) return false;
        if (!GuiTools.inArea(mx, my, posX, posY, posX + width, posY + height)) return false;
        int selected = (my - posY) / lineHeight;
        CUOption selectedOption;
        try {
            selectedOption = currentOptions.get(selected + currentOffset);
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        if (!selectedOption.checkClick(mx, my, parent)) {
            if (action != null) action.accept(selectedOption);
        }
        return true;
    }
}
