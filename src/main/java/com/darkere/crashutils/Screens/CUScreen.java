package com.darkere.crashutils.Screens;

import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.Network.Network;
import com.darkere.crashutils.Network.TeleportMessage;
import com.darkere.crashutils.Screens.Types.DropDownType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class CUScreen extends Screen {
    CUContentPane contentGUI;
    long doubleClickTimer;
    double oldClickX;
    double OldClickY;
    DimensionType dim;
    int centerX;
    int centerY;
    int activeTab = 0;
    int tabs = 2;
    private static final ResourceLocation WINDOW = new ResourceLocation(CrashUtils.MODID, "textures/gui/cuscreen.png");
    private static final ResourceLocation TABS = new ResourceLocation(CrashUtils.MODID, "textures/gui/tabs.png");
    public List<CUDropDown> dropDowns = new ArrayList<>();
    public List<CUDropDown> topDropDowns = new ArrayList<>();
    ExtendedButton button;

    public CUScreen(DimensionType dimension) {
        super(new StringTextComponent("CUScreen"));
        dim = dimension;
    }

    @Override
    protected void init() {
        centerY = height / 2;
        centerX = width / 2;
        contentGUI = new GridChunkGUI(this, dim);
        button = new ExtendedButton(centerX + 174, centerY - 101, 20, 10, String.valueOf(contentGUI.updateSpeed), (x) -> {
            contentGUI.shouldUpdate = !contentGUI.shouldUpdate;
            contentGUI.setUpdateSpeed();
        });
        this.addButton(button);
    }

    @Override
    public void renderBackground() {
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bindTexture(WINDOW);
        int i = (this.width - 400) / 2;
        int j = (this.height - 215) / 2;
        blit(i, j, 0, 0, 420, 240, 512, 512);
        renderTabs();

    }

    @Override
    public void render(int mx, int my, float p_render_3_) {
        renderBackground();
        centerX = width / 2;
        centerY = height / 2;
        fill(centerX + 173, centerY - 102, centerX + 195, centerY - 90, contentGUI.shouldUpdate ? 0xff51f542 : 0xfff54242);
        button.x = centerX + 174;
        button.y = centerY -101;
        button.renderButton(mx, my, p_render_3_);
        contentGUI.render(centerX, centerY);
        dropDowns.forEach(x -> x.render(centerX, centerY));
        topDropDowns.forEach(x -> x.render(centerX, centerY));
        renderToolTips(mx, my);
        super.render(mx, my, p_render_3_);
    }

    private void renderToolTips(int mx, int my) {
        List<String> tooltips = new ArrayList<>();
        //tooltips.add(mx+ " " + my);
        if(contentGUI.isMouseOver(mx,my,centerX,centerY)) {
            if (contentGUI instanceof GridChunkGUI) {
                GridChunkGUI gui = (GridChunkGUI) contentGUI;
                ChunkPos chunkPos = gui.getChunkFor(mx, my);
                tooltips.add("Chunk: X: " + chunkPos.x + " Z: " + chunkPos.z);
                String loc = gui.getLocFor(mx, my);
                tooltips.add("State: " + gui.getNameForLocationType(loc));
                StringBuilder builder = new StringBuilder();
                builder.append("Tickets: ");
                String tickets = gui.getTicketsFor(mx, my);
                builder.append(tickets == null ? "None" : tickets);
                tooltips.add(builder.toString());
                tooltips.add("Double click to teleport");
            } else if(contentGUI instanceof DataListGUI){
                DataListGUI gui = (DataListGUI) contentGUI;
                gui.addToToolTip(tooltips,mx,my);
            }
        }
        if(button.isMouseOver(mx,my)){
            tooltips.add("Requesting data every " + contentGUI.updateSpeed + " seconds");
            tooltips.add("Currently "+ (contentGUI.shouldUpdate ? "enabled" : "disabled"));
            tooltips.add("Scroll to change update Speed");
            tooltips.add("(It may be possible to lag a server using this)");
        }
        renderTooltip(tooltips, mx, my);
    }

    private void renderTabs() {
        int x = (this.width - 398) / 2;
        int y = (this.height - 257) / 2;
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bindTexture(TABS);
        for (int i = 0; i < tabs; i++) {
            if (i == 0) {
                if (i == activeTab) {
                    CUTab.ATL.drawTab(this, x + (i * 27), y);
                } else {
                    CUTab.ITL.drawTab(this, x + (i * 27), y);
                }
            } else {
                if (i == activeTab) {
                    CUTab.ATC.drawTab(this, x + (i * 27), y);
                } else {
                    CUTab.ITC.drawTab(this, x + (i * 27), y);
                }
            }
        }
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        DataHolder.setChunkDataFilter("");
        DataHolder.cancelTimer();
        DataHolder.stopListening();
    }

    @Override
    public boolean mouseDragged(double XStart, double YStart, int Button, double XDif, double YDif) {
        if (contentGUI.isMouseOver(XStart,YStart,centerX,centerY)) {
            contentGUI.addOffset(-XDif, -YDif);
            return true;
        }
        return super.mouseDragged(XStart, YStart, Button, XDif, YDif);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int mouseButton) {
        if (mouseButton != 0) return false;
        for (CUDropDown dropDown : topDropDowns) {
            if (dropDown.checkClick((int) mx, (int) my)) {
                return true;
            }
        }
        for (CUDropDown dropDown : dropDowns) {
            if (dropDown.checkClick((int) mx, (int) my)) {
                return true;
            }
        }
        if (my > centerY -126 && my < centerY -107) {
            return clickedTabArea(mx, my, mouseButton);
        }
        if (Instant.now().getEpochSecond() - doubleClickTimer < 1) {
            if (contentGUI.isMouseOver(mx,my,centerX,centerY)) {
                if (Math.sqrt(((oldClickX - mx) * (oldClickX - mx)) + ((OldClickY - my) * (OldClickY - my))) > 5)
                    return super.mouseClicked(mx, my, mouseButton);
                if (contentGUI instanceof GridChunkGUI) {
                    GridChunkGUI gui = (GridChunkGUI) contentGUI;
                    BlockPos pos = gui.getChunkFor((int) mx, (int) my).asBlockPos();
                    Network.INSTANCE.sendToServer(new TeleportMessage(dim, dim, pos));
                    return true;
                }
            }
        } else {
            doubleClickTimer = Instant.now().getEpochSecond();
            oldClickX = mx;
            OldClickY = my;
        }
        return super.mouseClicked(mx, my, mouseButton);

    }

    private boolean clickedTabArea(double mx, double my, int mouseButton) {
        int x = (int) mx - (centerX -198 );
        int tab = x / 27;
        if (tab >= tabs) return false;
        activeTab = tab;
        switchtabs();
        return true;

    }

    private void switchtabs() {
        dropDowns.clear();
        topDropDowns.clear();
        switch (activeTab) {
            case 0:
                contentGUI = new GridChunkGUI(this, dim);
                break;
            case 1:
                contentGUI = new DataListGUI(this, dim);
                break;
        }


    }

    @Override
    public boolean mouseScrolled(double mx, double my, double delta) {
        if (button.isMouseOver(mx, my)) {
            if (delta > 0 && contentGUI.updateSpeed < 5) {
                contentGUI.updateSpeed += 1;
            } else if (delta > 0 && contentGUI.updateSpeed < 60) {
                contentGUI.updateSpeed += 5;
            } else if (delta < 0 && contentGUI.updateSpeed > 5) {
                contentGUI.updateSpeed -= 5;
            } else if (delta < 0 && contentGUI.updateSpeed <= 5 && contentGUI.updateSpeed > 1) {
                contentGUI.updateSpeed -= 1;
            }
            button.setMessage(String.valueOf(contentGUI.updateSpeed));
            contentGUI.setUpdateSpeed();
            return true;
        }
        if (contentGUI.isMouseOver(mx, my, centerX, centerY)) {
            for (CUDropDown dropDown : topDropDowns) {
                if (dropDown.scroll(mx, my, delta)) {
                    return true;
                }
            }
            for (CUDropDown dropDown : dropDowns) {
                if (dropDown.scroll(mx, my, delta)) {
                    return true;
                }
            }
            contentGUI.zoom(mx, my, delta, centerX, centerY);
            return true;
        }
        return super.mouseScrolled(mx, my, delta);

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (CUDropDown dropDown : dropDowns) {
            if (dropDown.isEnabled() && dropDown.widget.keyPressed(keyCode, scanCode, modifiers)) {
                dropDown.updateFilter();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
        for (CUDropDown dropDown : dropDowns) {
            if (dropDown.isEnabled() && dropDown.widget.charTyped(p_charTyped_1_, p_charTyped_2_)) {
                dropDown.updateFilter();
                return true;
            }
        }
        return super.charTyped(p_charTyped_1_, p_charTyped_2_);
    }

    public void updateSelection(DropDownType ddtype, String s) {
        contentGUI.updateSelection(ddtype, s);
    }
}


