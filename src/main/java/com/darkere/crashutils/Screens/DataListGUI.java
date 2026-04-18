package com.darkere.crashutils.Screens;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.Network.DataRequestType;
import com.darkere.crashutils.Screens.Types.DropDownType;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Arrays;

public class DataListGUI extends CUContentPane {
    CUDropDown SELECTOR;
    DataListLoader loader;
    EditBox filterWidget;

    DataListGUI(CUScreen screen, ResourceKey<Level> dim) {
        super(dim, screen);
        updateRenderValues(screen.centerX, screen.centerY);
        loader = new DataListLoader(XTopLeft, YTopLeft, XAcross, YAcross, screen, dim);
        DataHolder.setRequestType(DataRequestType.ENTITYDATA);
        DataHolder.requestUpdates(0, dim, true);
        SELECTOR = new CUDropDown(DropDownType.SELECTOR, screen, Arrays.asList("ENTITIES", "BLOCKENTITIES", "TICKETS", "STATES", "PLAYERS"), "ENTITIES", -192, -105, 80);
        screen.topDropDowns.add(SELECTOR);
        SELECTOR.setEnabled(true);
        loader.loadOrderedEntityList(false);
        filterWidget = new EditBox(Minecraft.getInstance().font, centerX - 107, centerY - 104, 150, Minecraft.getInstance().font.lineHeight + 2, CommandUtils.CreateTextComponent("Filter")){
            @Override
            public boolean mouseClicked(MouseButtonEvent event,boolean doubleclick) {
                if(event.button() == InputConstants.MOUSE_BUTTON_RIGHT)
                    filterWidget.setValue("");
                setFocused(true);
                return super.mouseClicked(event, doubleclick);
            }
        };
        filterWidget.setResponder(r -> loader.currentList.updateFilter(r));
    }

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, int centerX, int centerY, int mx, int my, float partialTicks) {
        filterWidget.extractRenderState(guiGraphics, mx, my, partialTicks);
        super.render(guiGraphics, centerX, centerY, mx, my, partialTicks);
        loader.currentList.render(guiGraphics, mx, my, partialTicks);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleclick) {
        return loader.currentList.checkClick(event);
    }

    @Override
    public boolean mouseClickedOutside(MouseButtonEvent event, boolean doubleclick) {
        if(filterWidget.isMouseOver(event.x(),event.y()))
            return filterWidget.mouseClicked(event, doubleclick);
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent  keyEvent) {
        return filterWidget.keyPressed(keyEvent);
    }

    @Override
    public boolean charTyped(CharacterEvent characterEvent) {
        return filterWidget.charTyped(characterEvent);
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
                case "BLOCKENTITIES":
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

