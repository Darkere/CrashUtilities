package com.darkere.crashutils.DataStructures;

import com.darkere.crashutils.Screens.CUOption;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerData {
    List<String> playerNames = new ArrayList<>();

    public void createLists(List<ServerLevel> worlds) {
        playerNames = worlds.get(0).getServer().getProfileCache().getTopMRUProfiles(1000).map(e -> e.getProfile().getName()).collect(Collectors.toList());
    }

    public List<String> getPlayerNames(String requestingPlayer) {
        playerNames.remove(requestingPlayer);
        return playerNames;
    }

    public PlayerData() {
    }

    public PlayerData(List<String> names) {
        playerNames = names;
    }

    public List<CUOption> getCUPlayers(String requestingPlayer) {
        playerNames.remove(requestingPlayer);
        List<CUOption> list = new ArrayList<>();
        for (String name : playerNames) {
            list.add(new CUOption(name, !Minecraft.getInstance().hasSingleplayerServer() && Minecraft.getInstance().getCurrentServer() != null && Minecraft.getInstance().getCurrentServer().playerList != null &&  Minecraft.getInstance().getCurrentServer().playerList.stream().map(Component::getString).anyMatch(name::equals) ? "( online)" : null));
        }
        return list;
    }
}
