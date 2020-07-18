package com.darkere.crashutils.Screens;

import com.darkere.crashutils.DataStructures.DataHolder;
import com.darkere.crashutils.Network.DataRequestType;
import com.darkere.crashutils.Network.Network;
import com.darkere.crashutils.Network.PlayerInventoryRequestMessage;
import com.darkere.crashutils.Screens.Types.DropDownType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

import java.util.ArrayList;

public class PlayerListGUI extends CUContentPane {
    CUDropDown PLAYERLIST;

    public PlayerListGUI(CUScreen screen, RegistryKey<World> dim) {
        super(dim, screen);
        String playerName = Minecraft.getInstance().player.getName().getString();
        DataHolder.requestUpdates(DataRequestType.PLAYERDATA, 0, dim, true);
        PLAYERLIST = new CUDropDown(DropDownType.PLAYERLIST, screen, DataHolder.getLatestPlayerData() == null ? new ArrayList<>() : DataHolder.getLatestPlayerData().getPlayerNames(playerName), "", defaultRenderOffsetX, defaultRenderOffsetY, 0);
        screen.dropDowns.add(PLAYERLIST);
        PLAYERLIST.setEnabled(true);
        PLAYERLIST.setAlwaysExpanded();
        PLAYERLIST.setFitOnScreen(17);
        DataHolder.registerListener(() -> {
            PLAYERLIST.updateOptions(DataHolder.getLatestPlayerData() == null ? new ArrayList<>() : DataHolder.getLatestPlayerData().getPlayerNames(playerName));
        });

    }

    @Override
    public void updateSelection(DropDownType ddtype, String s) {
        Network.sendToServer(new PlayerInventoryRequestMessage(s));
    }
}
