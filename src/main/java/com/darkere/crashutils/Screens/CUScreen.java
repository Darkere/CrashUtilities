package com.darkere.crashutils.Screens;

import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.Network.Network;
import com.darkere.crashutils.Network.TeleportMessage;
import com.darkere.crashutils.Screens.Types.DropDownType;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class CUScreen extends Screen {
    CUContentPane contentGUI;
    long doubleClickTimer;
    double oldClickX;
    double OldClickY;
    RegistryKey<World> dim;
    int centerX;
    int centerY;
    int activeTab = 0;
    int tabs = 3;
    private static final ResourceLocation WINDOW = new ResourceLocation(CrashUtils.MODID, "textures/gui/cuscreen.png");
    private static final ResourceLocation TABS = new ResourceLocation(CrashUtils.MODID, "textures/gui/tabs.png");
    public List<CUDropDown> dropDowns = new ArrayList<>();
    public List<CUDropDown> topDropDowns = new ArrayList<>();
    ExtendedButton button;
    boolean dragging;
    BlockPos initial = null;

    public CUScreen(RegistryKey<World> worldKey, BlockPos position) {
        super(new StringTextComponent("CUScreen"));
        dim = worldKey;
        initial = position;
    }

    @Override
    protected void init() {
        centerY = height / 2;
        centerX = width / 2;
        contentGUI = new GridChunkGUI(this, dim, initial);
        button = new ExtendedButton(centerX + 174, centerY - 103, 20, 10, new StringTextComponent(String.valueOf(contentGUI.updateSpeed)), (x) -> {
            contentGUI.shouldUpdate = !contentGUI.shouldUpdate;
            contentGUI.setUpdateSpeed();
        });
        this.addButton(button);
    }

    @Override
    public void renderBackground(MatrixStack stack) {
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bindTexture(WINDOW);
        int i = centerX - (400 / 2);
        int j = centerY - (216 / 2);
        blit(stack,i, j, 0, 0, 400, 216, 512, 512);
        renderTabs(stack);

    }

    @Override
    public void render(MatrixStack stack, int mx, int my, float p_render_3_) {
        contentGUI.render(stack, centerX, centerY);
        dropDowns.forEach(x -> x.render(stack, centerX, centerY));
        renderBackground(stack);
        centerX = width / 2;
        centerY = height / 2;
        fill(stack, centerX + 173, centerY - 105, centerX + 195, centerY - 93, contentGUI.shouldUpdate ? 0xff51f542 : 0xfff54242);
        button.x = centerX + 174;
        button.y = centerY - 104;
        button.renderButton(stack, mx, my, p_render_3_);
        topDropDowns.forEach(x -> x.render(stack, centerX, centerY));
        renderToolTips(stack, mx, my);
        super.render(stack, mx, my, p_render_3_);
    }

    private void renderToolTips(MatrixStack stack, int mx, int my) {
        IFormattableTextComponent tooltips = new StringTextComponent("");
        //tooltips.add(mx+ " " + my);
        if (contentGUI.isMouseOver(mx, my, centerX, centerY)) {
            if (contentGUI instanceof GridChunkGUI) {
                GridChunkGUI gui = (GridChunkGUI) contentGUI;
                ChunkPos chunkPos = gui.getChunkFor(mx, my);
                tooltips.func_230529_a_(new StringTextComponent("Chunk: X: " + chunkPos.x + " Z: " + chunkPos.z));
                String loc = gui.getLocFor(mx, my);
                tooltips.func_230529_a_(new StringTextComponent("State: " + gui.getNameForLocationType(loc)));
                StringBuilder builder = new StringBuilder();
                switch (gui.type) {
                    case TICKET:
                    case LOCATIONTYPE:
                        builder.append("Tickets: ");
                        String tickets = gui.getTicketsFor(mx, my);
                        builder.append(tickets == null ? "None" : tickets);
                        break;
                    case ENTITIES:
                        builder.append("Entities: ");
                        String entities = gui.getEntityCountFor(mx, my);
                        builder.append(entities == null ? "None" : entities);
                        break;
                    case TILEENTITIES:
                        builder.append("Tileentities: ");
                        String tileEntities = gui.getTileEntityCountFor(mx, my);
                        builder.append(tileEntities == null ? "None" : tileEntities);
                        break;
                }

                tooltips.func_230529_a_(new StringTextComponent(builder.toString()));
                tooltips.func_230529_a_(new StringTextComponent("(Double click to teleport)"));
            } else if (contentGUI instanceof DataListGUI) {
                DataListGUI gui = (DataListGUI) contentGUI;
                gui.addToToolTip(tooltips, mx, my);
            }
        }
        if (button.isMouseOver(mx, my)) {
            tooltips.func_230529_a_(new StringTextComponent("Requesting data every " + contentGUI.updateSpeed + " seconds"));
            tooltips.func_230529_a_(new StringTextComponent("Currently " + (contentGUI.shouldUpdate ? "enabled" : "disabled")));
            tooltips.func_230529_a_(new StringTextComponent("Scroll to change update Speed"));
            tooltips.func_230529_a_(new StringTextComponent("(It may be possible to lag a server using this)"));
        }
        renderTooltip(stack, tooltips, mx, my);
    }

    private void renderTabs(MatrixStack stack) {
        int x = centerX - (400 / 2);
        int y = centerY - (216 / 2) - 22;
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bindTexture(TABS);
        float iconScale = 3.75f;
        List<CUTab> tabIcons = new ArrayList<>();
        tabIcons.add(CUTab.MAPTABICON);
        tabIcons.add(CUTab.LISTTABICON);
        tabIcons.add(CUTab.INVSEETABICON);
        for (int i = 0; i < tabs; i++) {
            if (i == 0) {
                if (i == activeTab) {
                    CUTab.ATL.drawTab(stack,this, x + (i * 27), y, tabIcons.get(i), iconScale);
                } else {
                    CUTab.ITL.drawTab(stack,this, x + (i * 27), y, tabIcons.get(i), iconScale);
                }
            } else {
                if (i == activeTab) {
                    CUTab.ATC.drawTab(stack,this, x + (i * 27), y, tabIcons.get(i), iconScale);
                } else {
                    CUTab.ITC.drawTab(stack,this, x + (i * 27), y, tabIcons.get(i), iconScale);
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
        if (dragging) {
            contentGUI.addOffset(-XDif, -YDif);
            return true;
        }
        return super.mouseDragged(XStart, YStart, Button, XDif, YDif);
    }

    @Override
    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        if (p_mouseReleased_5_ == 0) {
            dragging = false;
        }
        return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int mouseButton) {
        if (mouseButton != 0) return false;
        if (contentGUI.isMouseOver(mx, my, centerX, centerY)) {
            dragging = true;
        }
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
        if (my > centerY - 126 && my < centerY - 107) {
            return clickedTabArea(mx, my, mouseButton);
        }
        if (Instant.now().getEpochSecond() - doubleClickTimer < 1) {
            if (contentGUI.isMouseOver(mx, my, centerX, centerY)) {
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
        int x = (int) mx - (centerX - 198);
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
                contentGUI = new GridChunkGUI(this, dim, initial);
                break;
            case 1:
                contentGUI = new DataListGUI(this, dim);
                break;
            case 2:
                contentGUI = new PlayerListGUI(this, dim);
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
            button.setMessage(new StringTextComponent(String.valueOf(contentGUI.updateSpeed)));
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


