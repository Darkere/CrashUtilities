package com.darkere.crashutils.DataStructures;

import com.darkere.crashutils.CrashUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerActivityHistory {
    List<String> month = new ArrayList<>();
    List<String> week = new ArrayList<>();
    List<String> day = new ArrayList<>();
    List<String> playersInChunkClearTime = new ArrayList<>();
    private static final long monthTime = 2629743;
    private static final long weekTime = 604800;
    private static final long dayTime = 86400;
    private static final long chunkcleanertimer = dayTime * CrashUtils.SERVER_CONFIG.getExpireTimeInDays();


    public PlayerActivityHistory(ServerWorld world) {
        long current = Instant.now().getEpochSecond();
        try {
            Files.list(world.getServer().playerDataStorage.getPlayerDataFolder().toPath()).forEach(x -> {
                if (x.toFile().isDirectory()
                        || x.getFileName().toString().endsWith("old")
                        || !x.getFileName().toString().endsWith("dat")) {
                    return;
                }
                long fileTime = 0;
                try {
                    fileTime = Files.getLastModifiedTime(x, LinkOption.NOFOLLOW_LINKS).toInstant().getEpochSecond();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                long diff = current - fileTime;
                if (diff < monthTime) {
                    String uuidText = FilenameUtils.removeExtension(x.getFileName().toString());

                    UUID uuid;
                    try{
                        uuid = UUID.fromString(uuidText);
                    } catch (IllegalArgumentException ignored) {
                        return;
                    }

                    GameProfile profile = world.getServer().getProfileCache().get(uuid);
                    if (profile == null) return;
                    String playerName = profile.getName();
                    month.add(playerName);
                    if (diff < weekTime) {
                        week.add(playerName);
                        if (diff < dayTime) {
                            day.add(playerName);
                        }
                    }

                    if(diff > chunkcleanertimer)
                        playersInChunkClearTime.add(playerName);

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public List<String> getMonth() {
        return month;
    }

    public List<String> getWeek() {
        return week;
    }

    public List<String> getDay() {
        return day;
    }

    public List<String> getPlayersInChunkClearTime() {
        return playersInChunkClearTime;
    }
}
