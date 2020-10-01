package com.darkere.crashutils.Screens;

import com.darkere.crashutils.Screens.Types.DropDownType;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.text.StringTextComponent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CUDropDown extends AbstractGui {
    private List<String> allOptions;
    private List<String> options = new ArrayList<>();
    private String selected = "";
    private int posX;
    private int posY;
    private int width;
    private boolean expanded;
    private boolean alwaysExpanded;
    private List<FillMany.Text> strings;
    private final int height = 11;
    final TextFieldWidget widget;
    private boolean isEnabled = false;
    String oldFilter = "";
    CUScreen parent;
    DropDownType type;
    int fitOnScreen = 17;
    private int currentOffset = 0;
    private int maxOffset;
    private boolean variableLength = false;
    private final int defaultXRenderOffset;
    private final int defaultYRenderOffset;
    boolean sortByname = false;
    boolean allOption;

    public CUDropDown(DropDownType type, CUScreen parent, List<String> options, String selected, int defaultXRenderOffset, int defaultYRenderOffset, int width) {
        this.type = type;
        this.parent = parent;
        if (options == null) options = new ArrayList<>();
        this.allOptions = options;
        if (!allOptions.contains(selected)) allOptions.add(selected);
        this.selected = selected;
        allOption = selected.equals("all");
        this.defaultXRenderOffset = defaultXRenderOffset;
        this.defaultYRenderOffset = defaultYRenderOffset;
        this.posX = parent.centerX + defaultXRenderOffset + 1;
        this.posY = parent.centerY + defaultYRenderOffset + 1;
        this.options.addAll(options);
        this.options.remove(selected);
        if (width == 0) {
            variableLength = true;
            updateWidth();
        } else {
            this.width = width;
        }
        widget = new TextFieldWidget(Minecraft.getInstance().fontRenderer, this.posX, this.posY, this.width, height, new StringTextComponent(selected));
        widget.setText(selected);
        widget.setCursorPositionZero();
        maxOffset = options.size() - fitOnScreen;
        if (maxOffset < 0) maxOffset = 0;

    }

    public void setFitOnScreen(int fitOnScreen) {
        this.fitOnScreen = fitOnScreen;
    }

    public void render(MatrixStack stack, int centerX, int centerY) {
        posX = centerX + defaultXRenderOffset + 1;
        posY = centerY + defaultYRenderOffset + 1;
        if (!isEnabled) return;
        List<FillMany.ColoredRectangle> list = new ArrayList<>();
        strings = new ArrayList<>();
        widget.render(stack, posX, posY, 0);

        if (expanded) {
            boolean colored = false;
            int fits = Math.min(fitOnScreen, options.size());
            list.add(new FillMany.ColoredRectangle(posX - 1, posY, posX, posY + fits * height + height, -6250336));
            list.add(new FillMany.ColoredRectangle(posX + width, posY, posX + width + 1, posY + fits * height + height, -6250336));
            if (maxOffset > 0) {
                float percent = (float) currentOffset / (float) maxOffset;
                int pos = (int) (percent * (height * fits - height));
                list.add(new FillMany.ColoredRectangle(posX + width, posY + height + pos, posX + width + 1, posY + height + pos + height, -1));
            }
            for (int i = 0; i < fits; i++) {
                list.add(new FillMany.ColoredRectangle(posX, posY + (i + 1) * height, posX + width, posY + (i + 1) * height + height, colored ? 0xFF686868 : 0xFFA8A8A8));
                strings.add(new FillMany.Text(posX + 4, posY + (i + 1) * height + 2, options.get(i + currentOffset), -1));
                colored = !colored;
            }
        }
        FillMany.fillMany(TransformationMatrix.identity().getMatrix(), list);
        FillMany.drawStrings(stack, Minecraft.getInstance().fontRenderer, strings);

    }

    public void setSortByName(boolean sortByname) {
        this.sortByname = sortByname;
    }

    public void updateFilter() {
        if (!isEnabled) return;
        String f = widget.getText();
        options.clear();
        options.addAll(allOptions);
        if (allOption) options.add("all");
        if (!f.equals("all")) {
            options.removeIf(option -> {
                if (option.startsWith("[")) {
                    option = option.substring(option.indexOf("]") + 2);
                }
                return !option.startsWith(f) && !StringUtils.substringAfter(option, ":").startsWith(f);
            });
        }
        maxOffset = options.size() - fitOnScreen;
        if (sortByname) {
            options.sort(String::compareTo);
        }
        if (maxOffset < 0) maxOffset = 0;
        if (currentOffset > maxOffset) currentOffset = maxOffset;
        if (variableLength) {
            updateWidth();
        }
    }

    public void setAlwaysExpanded() {
        this.alwaysExpanded = true;
        this.setFitOnScreen(16);
        this.setSortByName(false);
        setExpanded(true);
    }

    private void updateWidth() {
        if (!allOptions.isEmpty()) {
            String longest = allOptions.stream().max(Comparator.comparingInt(String::length)).orElseGet(() -> "minecraft:baselength");
            width = Minecraft.getInstance().fontRenderer.getStringWidth(longest) + 4;
        } else {
            width = 30;
        }
        if (widget != null)
            widget.setWidth(width);
    }

    public void updateOptions(List<String> strings) {
        this.allOptions = strings;
        updateFilter();
    }

    public void setFilter(String filter) {
        widget.setText(filter);
        updateFilter();
    }

    public void setExpanded(boolean expanded) {
        if (!alwaysExpanded) {
            this.expanded = expanded;
            widget.setFocused2(expanded);
            widget.setEnabled(expanded);
        } else {
            this.expanded = true;
            widget.setFocused2(true);
            widget.setEnabled(true);
        }

    }

    public boolean isMouseOver(int mx, int my) {
        return GuiTools.inArea(mx, my, posX, posY + height, posX + width, posY + strings.size() * height + height);
    }

    public boolean checkClick(int x, int y) {
        if (!isEnabled) return false;
        if (alwaysExpanded) {
            if (GuiTools.inArea(x, y, posX, posY, posX + width, posY + height)) {
                setExpanded(true);
                oldFilter = widget.getText();
                widget.setText("");
                updateFilter();
            }
        }
        if (expanded) {
            if (!GuiTools.inArea(x, y, posX, posY + height, posX + width, posY + strings.size() * height + height)) {
                setExpanded(false);
                this.currentOffset = 0;
                if (widget.getText().isEmpty()) widget.setText(oldFilter);
                if (alwaysExpanded) return false;
                return true;
            } else {
                int se = y - posY;
                se /= 11;
                if (options.size() >= se) {
                    if (!alwaysExpanded) {
                        if (!selected.isEmpty()) {
                            options.add(selected);
                        }
                        selected = options.get(se - 1 + currentOffset);
                        widget.setText(selected);
                        options.remove(selected);
                        parent.updateSelection(type, selected);
                    } else {
                        parent.updateSelection(type, options.get(se - 1));
                    }
                    setExpanded(false);
                    this.currentOffset = 0;
                    return true;
                }
            }
        } else {
            if (GuiTools.inArea(x, y, posX, posY, posX + width, posY + height)) {
                setExpanded(true);
                oldFilter = widget.getText();
                widget.setText("");
                updateFilter();

                return true;
            }
        }
        return false;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }


    public boolean scroll(double mx, double my, double delta) {
        if (!isEnabled) return false;
        if (GuiTools.inArea((int) mx, (int) my, posX, posY + height, posX + width, posY + strings.size() * height + height)) {
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
}
