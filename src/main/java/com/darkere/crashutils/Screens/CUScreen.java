package com.darkere.crashutils.Screens;

import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.Network.DataRequestType;
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
    public static CUScreen stored;
    CUContentPane contentGUI;
    long doubleClickTimer;
    double oldClickX;
    double OldClickY;
    RegistryKey<World> dim;
    int centerX;
    int centerY;
    int activeTab = 0;
    int tabs = 2;
    private static final ResourceLocation WINDOW = new ResourceLocation(CrashUtils.MODID, "textures/gui/cuscreen.png");
    private static final ResourceLocation TABS = new ResourceLocation(CrashUtils.MODID, "textures/gui/tabs.png");
    public List<CUDropDown> topDropDowns = new ArrayList<>();
    ExtendedButton button;
    boolean dragging;
    BlockPos initial = null;
    static boolean keep = false;

    public CUScreen(RegistryKey<World> worldKey, BlockPos position) {
        super(new StringTextComponent("CUScreen"));
        dim = worldKey;
        initial = position;

    }

    @Override
    protected void init() {
        centerY = height / 2;
        centerX = width / 2;
        if(!keep){
            contentGUI = new MapGUI(this, dim, initial);
            DataHolder.setRequestType(DataRequestType.LOADEDCHUNKDATA);
            button = new ExtendedButton(centerX + 174, centerY - 103, 20, 10, new StringTextComponent(String.valueOf(contentGUI.updateSpeed)),
                (x) -> {
                contentGUI.shouldUpdate = !contentGUI.shouldUpdate;
                contentGUI.setUpdateSpeed();
            });
            this.addButton(button);
        }
    }

    @Override
    public void renderBackground(MatrixStack stack) {
        assert this.minecraft != null;
        this.minecraft.getTextureManager().bindTexture(WINDOW);
        int i = centerX - (400 / 2);
        int j = centerY - (216 / 2);
        blit(stack, i, j, 0, 0, 400, 216, 512, 512);
        renderTabs(stack);

    }

    @Override
    public void render(MatrixStack stack, int mx, int my, float partialTicks) {
        renderBackground(stack);
        contentGUI.render(stack, centerX, centerY,mx,my, partialTicks);
        centerX = width / 2;
        centerY = height / 2;
        fill(stack, centerX + 173, centerY - 105, centerX + 195, centerY - 93, contentGUI.shouldUpdate ? 0xff51f542 : 0xfff54242);
        button.x = centerX + 174;
        button.y = centerY - 104;

        topDropDowns.forEach(x -> x.render(stack, centerX, centerY));
        renderToolTips(stack, mx, my);
        button.renderButton(stack, mx, my, partialTicks);
        super.render(stack, mx, my, partialTicks);
    }

    private void renderToolTips(MatrixStack stack, int mx, int my) {
        IFormattableTextComponent tooltips = new StringTextComponent("");
        //tooltips.add(mx+ " " + my);
        if (contentGUI.isMouseOver(mx, my, centerX, centerY)) {
            if (contentGUI instanceof MapGUI) {
                MapGUI gui = (MapGUI) contentGUI;
                ChunkPos chunkPos = gui.getChunkFor(mx, my);
                tooltips.append(new StringTextComponent("Chunk: X: " + chunkPos.x + " Z: " + chunkPos.z + "\n"));
                String loc = gui.getLocFor(mx, my);
                tooltips.append(new StringTextComponent("State: " + gui.getNameForLocationType(loc) + "\n"));
                StringBuilder builder = new StringBuilder();
                switch (gui.type) {
                    case TICKET:
                    case LOCATIONTYPE:
                        builder.append("Tickets: ");
                        String tickets = gui.getTicketsFor(mx, my);
                        builder.append(tickets == null ? "None" : tickets).append("\n");
                        break;
                    case ENTITIES:
                        builder.append("Entities: ");
                        String entities = gui.getEntityCountFor(mx, my);
                        builder.append(entities == null ? "None" : entities).append("\n");
                        break;
                    case TILEENTITIES:
                        builder.append("Tileentities: ");
                        String tileEntities = gui.getTileEntityCountFor(mx, my);
                        builder.append(tileEntities == null ? "None" : tileEntities).append("\n");
                        break;
                }

                tooltips.append(new StringTextComponent(builder.toString()));
                tooltips.append(new StringTextComponent("(Double click to teleport)"));
            }
        }
        if (button.isMouseOver(mx, my)) {
            tooltips.append(new StringTextComponent("Requesting data every " + contentGUI.updateSpeed + " seconds" + "\n"));
            tooltips.append(new StringTextComponent("Currently " + (contentGUI.shouldUpdate ? "enabled" : "disabled") + "\n"));
            tooltips.append(new StringTextComponent("Scroll to change update Speed" + "\n"));
            tooltips.append(new StringTextComponent("(It may be possible to lag a server using this)"));
        }
        if (!tooltips.getString().isEmpty()) {
            renderTooltip(stack, tooltips, mx, my);
        }
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
                    CUTab.ATL.drawTab(stack, this, x + (i * 27), y, tabIcons.get(i), iconScale);
                } else {
                    CUTab.ITL.drawTab(stack, this, x + (i * 27), y, tabIcons.get(i), iconScale);
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
    public void onClose() {
        super.onClose();
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
        if(contentGUI.mouseClickedOutside(mx,my,centerX,centerY)){
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
            contentGUI.scroll(mx, my, delta, centerX, centerY);
            return true;
        }
        return super.mouseScrolled(mx, my, delta);

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if(contentGUI.keyPressed(keyCode,scanCode,modifiers)){
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
         if(contentGUI.charTyped(p_charTyped_1_,p_charTyped_2_)){
            return true;
        }
        return super.charTyped(p_charTyped_1_, p_charTyped_2_);
    }

    public void updateSelection(DropDownType ddtype, String s) {
        contentGUI.updateSelection(ddtype, s);
    }

    public static CUScreen openCUScreen(RegistryKey<World> world,BlockPos pos ){
        if(stored == null || !keep){
            stored = new CUScreen(world,pos);
        }
        DataHolder.notifyListener();
        return stored;
    }
}


