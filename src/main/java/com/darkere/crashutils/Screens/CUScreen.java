package com.darkere.crashutils.Screens;

import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.Network.DataRequestType;
import com.darkere.crashutils.Network.Network;
import com.darkere.crashutils.Network.TeleportMessage;
import com.darkere.crashutils.Screens.Types.DropDownType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.gui.widget.ExtendedButton;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


public class CUScreen extends Screen {
    public static CUScreen stored;
    CUContentPane contentGUI;
    long doubleClickTimer;
    double oldClickX;
    double OldClickY;
    ResourceKey<Level> dim;
    int centerX;
    int centerY;
    int activeTab = 0;
    int tabs = 2;
    private static final ResourceLocation WINDOW = new ResourceLocation(CrashUtils.MODID, "textures/gui/cuscreen.png");
    private static final ResourceLocation TABS = new ResourceLocation(CrashUtils.MODID, "textures/gui/tabs.png");
    public List<CUDropDown> topDropDowns = new ArrayList<>();
    ExtendedButton button;
    ExtendedButton backButton;
    boolean dragging;
    BlockPos initial;
    static boolean keep = false;

    public CUScreen(ResourceKey<Level> worldKey, BlockPos position) {
        super(new TextComponent("CUScreen"));
        dim = worldKey;
        initial = position;

    }

    @Override
    protected void init() {
        super.init();
        centerY = height / 2;
        centerX = width / 2;
        if (!keep) {
            contentGUI = new MapGUI(this, dim, initial);
            DataHolder.setRequestType(DataRequestType.LOADEDCHUNKDATA);
        }
            button = new ExtendedButton(centerX + 174, centerY - 103, 20, 10, new TextComponent(String.valueOf(contentGUI.updateSpeed)),
                (x) -> {
                    contentGUI.shouldUpdate = !contentGUI.shouldUpdate;
                    contentGUI.setUpdateSpeed();
                });
            this.addWidget(button);
            backButton = new ExtendedButton(centerX + 145, centerY - 103, 20,10 ,new TextComponent("<-"),
            button->{
                if(contentGUI instanceof DataListGUI)
                    ((DataListGUI) contentGUI).loader.goBack();
                else if (contentGUI instanceof MapGUI)
                    ((MapGUI)contentGUI).goTo(((MapGUI)contentGUI).initial);


            });
            addWidget(backButton);
    }

    @Override
    public void renderBackground(PoseStack stack) {
        assert this.minecraft != null;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WINDOW);
        int i = centerX - (400 / 2);
        int j = centerY - (216 / 2);
        blit(stack, i, j, 0, 0, 400, 216, 512, 512);
        renderTabs(stack);

    }

    @Override
    public void render(PoseStack stack, int mx, int my, float partialTicks) {
        renderBackground(stack);
        contentGUI.render(stack, centerX, centerY, mx, my, partialTicks);
        centerX = width / 2;
        centerY = height / 2;
        fill(stack, centerX + 173, centerY - 105, centerX + 195, centerY - 93, contentGUI.shouldUpdate ? 0xff51f542 : 0xfff54242);
        button.x = centerX + 174;
        button.y = centerY - 104;
        backButton.x = centerX + 145;
        backButton.y = centerY - 103;

        topDropDowns.forEach(x -> x.render(stack, centerX, centerY));
        renderToolTips(stack, mx, my);
        button.renderButton(stack, mx, my, partialTicks);
        backButton.renderButton(stack, mx, my, partialTicks);
        super.render(stack, mx, my, partialTicks);
    }

    private void renderToolTips(PoseStack stack, int mx, int my) {
        List<MutableComponent> tooltips = new ArrayList<>();
        //tooltips.add(mx+ " " + my);
        if (contentGUI.isMouseOver(mx, my, centerX, centerY)) {
            if (contentGUI instanceof MapGUI) {
                MapGUI gui = (MapGUI) contentGUI;
                ChunkPos chunkPos = gui.getChunkFor(mx, my);
                tooltips.add(new TextComponent("Chunk: X: " + chunkPos.x + " Z: " + chunkPos.z));
                String loc = gui.getLocFor(mx, my);
                tooltips.add(new TextComponent("State: " + gui.getNameForLocationType(loc)));
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

                tooltips.add(new TextComponent(builder.toString()));
                tooltips.add(new TextComponent("(Double click to teleport)"));
            }
        }
        if (button.isMouseOver(mx, my)) {
            tooltips.add(new TextComponent("Requesting data every " + contentGUI.updateSpeed + " seconds"));
            tooltips.add(new TextComponent("Currently " + (contentGUI.shouldUpdate ? "enabled" : "disabled")));
            tooltips.add(new TextComponent("Scroll to change update Speed"));
            tooltips.add(new TextComponent("(It may be possible to lag a server using this)"));
        }
        if(backButton.isMouseOver(mx,my)){
            tooltips.add(new TextComponent(contentGUI instanceof  MapGUI ? "Return to player" : "Go Back"));
        }
        if (!tooltips.isEmpty()) {
         //TODO:Fix Tooltips   renderTooltip(stack, (List<? extends FormattedCharSequence>) tooltips, mx, my, Minecraft.getInstance().font);
        }
    }

    private void renderTabs(PoseStack stack) {
        int x = centerX - (400 / 2);
        int y = centerY - (216 / 2) - 22;
        assert this.minecraft != null;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TABS);
        float iconScale = 3.75f;
        List<CUTab> tabIcons = new ArrayList<>();
        tabIcons.add(CUTab.MAPTABICON);
        tabIcons.add(CUTab.LISTTABICON);
        tabIcons.add(CUTab.INVSEETABICON);
        for (int i = 0; i < tabs; i++) {
            if (i == 0) {
                if (i == activeTab) {
                    CUTab.ATL.drawTab(stack, this, x, y, tabIcons.get(i), iconScale);
                } else {
                    CUTab.ITL.drawTab(stack, this, x, y, tabIcons.get(i), iconScale);
                }
            } else {
                if (i == activeTab) {
                    CUTab.ATC.drawTab(stack, this, x + (i * 27), y, tabIcons.get(i), iconScale);
                } else {
                    CUTab.ITC.drawTab(stack, this, x + (i * 27), y, tabIcons.get(i), iconScale);
                }
            }
        }
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        super.removed();
        DataHolder.cancelTimer();
        DataHolder.stopListening();
        stored = this;
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
        for (CUDropDown dropDown : topDropDowns) {
            if (dropDown.checkClick((int) mx, (int) my)) {
                return true;
            }
        }
        if (contentGUI.mouseClickedOutside(mx, my, centerX, centerY)) {
            return true;
        }

        if (contentGUI.isMouseOver(mx, my, centerX, centerY)) {
            dragging = true;
            if ((contentGUI.mouseClicked(mx, my, mouseButton))) {
                return true;
            }
        }
        if (my > centerY - 126 && my < centerY - 107) {
            return clickedTabArea(mx, my, mouseButton);
        }
        if (Instant.now().getEpochSecond() - doubleClickTimer < 1) {

            //TODO move to GridChunkGUI
            if (contentGUI.isMouseOver(mx, my, centerX, centerY)) {
                if (Math.sqrt(((oldClickX - mx) * (oldClickX - mx)) + ((OldClickY - my) * (OldClickY - my))) > 5)
                    return super.mouseClicked(mx, my, mouseButton);
                if (contentGUI instanceof MapGUI) {
                    MapGUI gui = (MapGUI) contentGUI;
                    BlockPos pos = gui.getChunkFor((int) mx, (int) my).getWorldPosition();
                    Network.sendToServer(new TeleportMessage(dim, dim, pos));
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
        switchTabs();
        return true;

    }

    private void switchTabs() {
        topDropDowns.clear();
        switch (activeTab) {
            case 0:
                contentGUI = new MapGUI(this, dim, initial);
                keep = false;
                break;
            case 1:
                contentGUI = new DataListGUI(this, dim);
                keep = true;
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
            button.setMessage(new TextComponent(String.valueOf(contentGUI.updateSpeed)));
            contentGUI.setUpdateSpeed();
            return true;
        }
        if (contentGUI.isMouseOver(mx, my, centerX, centerY)) {
            for (CUDropDown dropDown : topDropDowns) {
                if (dropDown.scroll(mx, my, delta)) {
                    return true;
                }
            }
            contentGUI.scroll(mx, my, delta, centerX, centerY);
            return true;
        }
        return super.mouseScrolled(mx, my, delta);

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (contentGUI.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
        if (contentGUI.charTyped(p_charTyped_1_, p_charTyped_2_)) {
            return true;
        }
        return super.charTyped(p_charTyped_1_, p_charTyped_2_);
    }

    public void updateSelection(DropDownType ddtype, String s) {
        contentGUI.updateSelection(ddtype, s);
    }

    public static CUScreen openCUScreen(ResourceKey<Level> world, BlockPos pos) {
        if (stored == null || !keep || !world.equals(stored.dim)) {
            keep = false;
            stored = new CUScreen(world, pos);
        }

        DataHolder.notifyListener();
        return stored;
    }
}


