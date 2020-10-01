package com.darkere.crashutils.DataStructures;

import com.darkere.crashutils.Screens.CUOption;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerData {
    List<String> playerNames = new ArrayList<>();

    public void createLists(List<ServerWorld> worlds) {
        playerNames = worlds.get(0).getServer().getPlayerProfileCache().func_242117_a(1000).map(e -> e.getGameProfile().getName()).collect(Collectors.toList());
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
        playerNames.forEach(name -> list.add(new CUOption(name, !Minecraft.getInstance().isSingleplayer() &&
            Minecraft.getInstance().getCurrentServerData().playerList.stream().map(ITextComponent::getString).anyMatch(name::equals) ? "(online)" : null)));
        return list;
    }
}
