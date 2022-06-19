package com.darkere.crashutils.DataStructures;


import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.Screens.CUOption;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.*;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ProtoChunk;

import java.util.*;

public class LoadedChunkData {
    Map<String, Set<ChunkPos>> chunksByTicketName = new HashMap<>();
    Map<String, Set<ChunkPos>> chunksByLocationType = new HashMap<>();
    Map<ChunkPos, Set<String>> ticketsByChunk = new HashMap<>();
    Map<ChunkPos, String> locationTypeByChunk = new HashMap<>();
    Map<String, LocationTickets> ticketsByLocation = new HashMap<>();

    int total = 0;

    public Map<String, Set<ChunkPos>> getChunksByTicketName() {
        return chunksByTicketName;
    }

    public Map<String, Set<ChunkPos>> getChunksByLocationType() {
        return chunksByLocationType;
    }

    public LoadedChunkData(Map<String, Set<ChunkPos>> chunksByTicketName, Map<String, Set<ChunkPos>> chunksByLocationType) {
        this.chunksByTicketName = chunksByTicketName;
        this.chunksByLocationType = chunksByLocationType;
    }

    public LoadedChunkData(List<ServerLevel> worlds) {
        CrashUtils.runNextTick((world) -> init(worlds));

    }

    public void init(List<ServerLevel> worlds) {
        for (Level world : worlds) {
            ChunkMap chunkManager = ((ServerChunkCache) world.getChunkSource()).chunkMap;
            DistanceManager ticketManager = chunkManager.getDistanceManager();
            total += chunkManager.size();
            Iterable<ChunkHolder> chunkHolders = chunkManager.getChunks();
            chunkHolders.forEach(chunkHolder -> {
                ChunkAccess chunk = chunkHolder.getLastAvailable();
                LocationTickets ticketCounter = null;
                if (chunk == null) {
                    chunksByLocationType.merge("PRIMED", new HashSet<>(Collections.singletonList(chunkHolder.getPos())), (list, newer) -> {
                        list.add(chunkHolder.getPos());
                        return list;
                    });
                    ticketsByLocation.merge("PRIMED", new LocationTickets(), (x, y) -> {
                        x.count++;
                        return x;
                    });
                    ticketCounter = ticketsByLocation.get("PRIMED");
                } else {
                    if (chunk instanceof LevelChunk) {
                        LevelChunk actualChunk = (LevelChunk) chunk;
                        chunksByLocationType.merge(actualChunk.getFullStatus().toString(), new HashSet<>(Collections.singletonList(chunkHolder.getPos())), (list, newer) -> {
                            list.add(chunkHolder.getPos());
                            return list;
                        });
                        ticketsByLocation.merge(actualChunk.getFullStatus().toString(), new LocationTickets(), (x, y) -> {
                            x.count++;
                            return x;
                        });
                        ticketCounter = ticketsByLocation.get(actualChunk.getFullStatus().toString());
                    } else {
                        if (chunk instanceof ProtoChunk) {
                            chunksByLocationType.merge(chunk.getStatus().getName().equals("full") ? "FULL" : "PARTIALLYGENERATED", new HashSet<>(Collections.singletonList(chunkHolder.getPos())), (list, newer) -> {
                                list.add(chunkHolder.getPos());
                                return list;
                            });
                            ticketsByLocation.merge(chunk.getStatus().getName().equals("full") ? "FULL" : "PARTIALLYGENERATED", new LocationTickets(), (x, y) -> {
                                x.count++;
                                return x;
                            });
                            ticketCounter = ticketsByLocation.get(chunk.getStatus().getName().equals("full") ? "FULL" : "PARTIALLYGENERATED");
                        }

                    }
                }
                SortedArraySet<Ticket<?>> tickets = ticketManager.getTickets(chunkHolder.getPos().toLong());
                if (tickets.isEmpty()) {
                    chunksByTicketName.merge("no_ticket", new HashSet<>(Collections.singletonList(chunkHolder.getPos())), (old, nothing) -> {
                        old.add(chunkHolder.getPos());
                        return old;
                    });
                } else {
                    for (Ticket<?> ticket : tickets) {
                        chunksByTicketName.merge(ticket.getType().toString(), new HashSet<>(Collections.singletonList(chunkHolder.getPos())), (old, nothing) -> {
                            old.add(chunkHolder.getPos());
                            return old;
                        });
                        if(ticketCounter != null)
                            ticketCounter.tickets.merge(ticket.getType().toString(),1,Integer::sum);
                    }
                }

            });
        }
    }

    public void reply(CommandSourceStack source) {
        ticketsByLocation.forEach((name,locationticket)->{
            source.sendSuccess(CommandUtils.CreateTextComponent(name + ": " + locationticket.count),true);
            locationticket.tickets.forEach((ticket,count)->{
                source.sendSuccess(CommandUtils.CreateTextComponent("    " + ticket + ": " + count), true);
            });
        });
        source.sendSuccess(CommandUtils.CreateTextComponent("Non-Ticking chunks have little to no performance impact. See the GUI and minecraft wiki for what each type represents."), false);
    }

    public void replyWithLocation(CommandSourceStack source, String word) throws CommandSyntaxException {
        source.sendSuccess(CommandUtils.CreateTextComponent("Chunks with LocationType " + word), true);
        Set<ChunkPos> chunkPos = chunksByLocationType.get(word);
        if (chunkPos != null) {
            sendChunkPositions(source, chunkPos);
        }

    }

    private void sendChunkPositions(CommandSourceStack source, Set<ChunkPos> chunks) throws CommandSyntaxException {
        for (ChunkPos chunkPos : chunks) {
            BlockPos pos = chunkPos.getWorldPosition();
            CommandUtils.sendCommandMessage(source, CommandUtils.CreateTextComponent(chunkPos.toString()), "/cu tp " + (source.getEntity() instanceof Player ? source.getPlayerOrException().getName().getString() : "Console") + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ(), true);
        }
    }

    public void replyWithTicket(CommandSourceStack source, String word) throws CommandSyntaxException {
        Set<ChunkPos> chunks = chunksByTicketName.get(word);
        if (chunks == null) return;
        source.sendSuccess(CommandUtils.CreateTextComponent("Chunks with " + word + " Ticket"), true);
        sendChunkPositions(source, chunks);
    }

    public void createReverseMapping() {
        chunksByTicketName.forEach((x, y) -> {
            y.forEach(t -> {
                ticketsByChunk.merge(t, new HashSet<>(Collections.singleton(x)), (old, n) -> {
                    old.add(x);
                    return old;
                });
            });
        });
        chunksByLocationType.forEach((x, y) -> {
            y.forEach(t -> {
                locationTypeByChunk.put(t, x);
            });
        });
    }

    public String getTickets(ChunkPos pos) {
        Set<String> strings = ticketsByChunk.get(pos);
        if (strings == null || strings.isEmpty()) return null;
        if (strings.size() > 1) strings.remove("unknown");
        StringBuilder toReturn = new StringBuilder();
        Iterator<String> it = strings.iterator();
        while (it.hasNext()) {
            toReturn.append(it.next());
            if (it.hasNext()) toReturn.append(", ");
        }
        return toReturn.toString();
    }

    public String getLocationType(ChunkPos pos) {
        return locationTypeByChunk.get(pos);
    }


    public List<CUOption> getTicketsAsDropdownOptions(String filter) {
        return getCuDropDownOptions(filter, chunksByTicketName);
    }

    public List<CUOption> getStatesAsDropdownOptions(String filter) {
        return getCuDropDownOptions(filter, chunksByLocationType);
    }

    private List<CUOption> getCuDropDownOptions(String filter, Map<String, Set<ChunkPos>> chunkMap) {
        List<CUOption> list = new ArrayList<>();
        if (filter.isEmpty()) {
            chunkMap.forEach((string, set) -> list.add(new CUOption(string, set.size())));
        } else {
            chunkMap.get(filter).forEach(chunkPos -> list.add(new CUOption(chunkPos)));
        }
        return list;
    }

    private static class LocationTickets {
        int count;
        Map<String, Integer> tickets = new HashMap<>();

    }
}

