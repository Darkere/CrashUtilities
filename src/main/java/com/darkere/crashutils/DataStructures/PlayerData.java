package com.darkere.crashutils.DataStructures;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PlayerData {
    List<String> playerNames = new ArrayList<>();

    public void createLists(List<ServerWorld> worlds) {
        playerNames = worlds.get(0).getServer().getPlayerProfileCache().gameProfiles.stream().map(GameProfile::getName).collect(Collectors.toList());
    }
    public List<String> getPlayerNames(String requestingPlayer){
        playerNames.remove(requestingPlayer);
        return playerNames;
    }
    public PlayerData(){}
    public PlayerData(List<String> names){
        playerNames = names;
    }
}
