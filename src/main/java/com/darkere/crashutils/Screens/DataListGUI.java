package com.darkere.crashutils.Screens;

import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.Network.DataRequestType;
import com.darkere.crashutils.Screens.Types.DropDownType;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.Arrays;

public class DataListGUI extends CUContentPane {
    CUDropDown SELECTOR;
    DataListLoader loader;
    TextFieldWidget filterWidget;

    DataListGUI(CUScreen screen, RegistryKey<World> dim) {
        super(dim, screen);
        updateRenderValues(screen.centerX, screen.centerY);
        loader = new DataListLoader(XTopLeft, YTopLeft, XAcross, YAcross, screen, dim);
        DataHolder.setRequestType(DataRequestType.ENTITYDATA);
        DataHolder.requestUpdates(0, dim, true);
        SELECTOR = new CUDropDown(DropDownType.SELECTOR, screen, Arrays.asList("ENTITIES", "TILEENTITIES", "TICKETS", "STATES", "PLAYERS"), "ENTITIES", -192, -105, 75);
        screen.topDropDowns.add(SELECTOR);
        SELECTOR.setEnabled(true);
        loader.loadOrderedEntityList(false);
        filterWidget = new TextFieldWidget(Minecraft.getInstance().fontRenderer, centerX - 107, centerY - 104, 150, Minecraft.getInstance().fontRenderer.FONT_HEIGHT+2, new StringTextComponent("Filter"));
        filterWidget.setResponder(r -> loader.currentList.updateFilter(r));
    }

    @Override
    public void render(MatrixStack stack, int centerX, int centerY, int mx, int my, float partialTicks) {
        super.render(stack, centerX, centerY, mx, my, partialTicks);
        loader.currentList.render(stack, mx, my, partialTicks);
        filterWidget.render(stack, mx, my, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int mouseButton) {
        return loader.currentList.checkClick((int) mx, (int) my);
    }

    @Override
    public boolean mouseClickedOutside(double mx, double my, int centerX, int centerY) {
        return filterWidget.mouseClicked(mx, my, 0);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return filterWidget.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
        return filterWidget.charTyped(p_charTyped_1_, p_charTyped_2_);
    }

    @Override
    public void scroll(double x, double y, double delta, int centerX, int centerY) {
        loader.currentList.scroll(x, y, delta);
    }

    @Override
    public void updateSelection(DropDownType ddtype, String s) {
        if (ddtype == DropDownType.SELECTOR) {
            switch (s) {
                case "ENTITIES":
                    DataHolder.setRequestType(DataRequestType.ENTITYDATA);
                    DataHolder.requestUpdates(0, screen.dim, !firstEntity);
                    currentType = DataRequestType.ENTITYDATA;
                    firstEntity = true;
                    loader.loadOrderedEntityList(false);
                    break;
                case "TILEENTITIES":
                    DataHolder.setRequestType(DataRequestType.TILEENTITYDATA);
                    DataHolder.requestUpdates(0, screen.dim, !firstTileEntity);
                    currentType = DataRequestType.TILEENTITYDATA;
                    firstTileEntity = true;
                    loader.loadOrderedTileEntityList(false);
                    break;
                case "TICKETS":
                    DataHolder.setRequestType(DataRequestType.LOADEDCHUNKDATA);
                    DataHolder.requestUpdates(0, screen.dim, !firstChunks);
                    firstChunks = true;
                    loader.loadTicketList(false);
                    break;
                case "STATES":
                    DataHolder.setRequestType(DataRequestType.LOADEDCHUNKDATA);
                    DataHolder.requestUpdates(0, screen.dim, !firstChunks);
                    firstChunks = true;
                    loader.loadStateList(false);
                    break;
                case "PLAYERS":
                    DataHolder.setRequestType(DataRequestType.PLAYERDATA);
                    DataHolder.requestUpdates(0, screen.dim, !firstPlayer);
                    firstPlayer = true;
                    loader.loadPlayerList(false);
                    break;
            }
        }
    }
}

