package com.darkere.crashutils.DataStructures;


import com.darkere.crashutils.CommandUtils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SortedArraySet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.*;

import java.util.*;
import java.util.stream.Collectors;

public class LoadedChunkData {
    Map<String, Set<ChunkPos>> chunksByTicketName = new HashMap<>();
    Map<String, Set<ChunkPos>> chunksByLocationType = new HashMap<>();
    Map<ChunkPos, Set<String>> ticketsByChunk = new HashMap<>();
    Map<ChunkPos, String> locationTypeByChunk = new HashMap<>();
    String filter = "";

    int total = 0;

    public Map<String, Set<ChunkPos>> getChunksByTicketName() {
        return chunksByTicketName;
    }


    public Map<ChunkPos, String> getLocationTypeByChunk() {
        if (!filter.isEmpty()) {
            return locationTypeByChunk.entrySet().stream().filter(e -> e.getValue().equals(filter)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        return locationTypeByChunk;
    }

    public Map<ChunkPos, Set<String>> getTicketsByChunk() {
        if (!filter.isEmpty()) {
            return ticketsByChunk.entrySet().stream().filter(e -> e.getValue().contains(filter)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        return ticketsByChunk;
    }

    public Map<String, Set<ChunkPos>> getChunksByLocationType() {
        return chunksByLocationType;
    }

    public LoadedChunkData(Map<String, Set<ChunkPos>> chunksByTicketName, Map<String, Set<ChunkPos>> chunksByLocationType) {
        this.chunksByTicketName = chunksByTicketName;
        this.chunksByLocationType = chunksByLocationType;
    }

    public LoadedChunkData(List<ServerWorld> worlds) {
        for (World world : worlds) {
            ChunkManager chunkManager = ((ServerChunkProvider) world.getChunkProvider()).chunkManager;
            TicketManager ticketManager = chunkManager.getTicketManager();
            total += chunkManager.getLoadedChunkCount();
            Iterable<ChunkHolder> chunkHolders = chunkManager.getLoadedChunksIterable();
            chunkHolders.forEach(chunkHolder -> {
                IChunk chunk = chunkHolder.func_219287_e();
                if (chunk == null) {
                    chunksByLocationType.merge("PRIMED", new HashSet<>(Collections.singletonList(chunkHolder.getPosition())), (list, newer) -> {
                        list.add(chunkHolder.getPosition());
                        return list;
                    });
                } else {
                    if (chunk instanceof Chunk) {
                        Chunk actualChunk = (Chunk) chunk;
                        chunksByLocationType.merge(actualChunk.getLocationType().toString(), new HashSet<>(Collections.singletonList(chunkHolder.getPosition())), (list, newer) -> {
                            list.add(chunkHolder.getPosition());
                            return list;
                        });
                    } else {
                        if (chunk instanceof ChunkPrimer) {
                            chunksByLocationType.merge(chunk.getStatus().getName().equals("full") ? "FULL" : "PARTIALLYGENERATED", new HashSet<>(Collections.singletonList(chunkHolder.getPosition())), (list, newer) -> {
                                list.add(chunkHolder.getPosition());
                                return list;
                            });
                        }

                    }
                }
                SortedArraySet<Ticket<?>> tickets = ticketManager.getTicketSet(chunkHolder.getPosition().asLong());
                if (tickets.isEmpty()) {
                    chunksByTicketName.merge("no_ticket", new HashSet<>(Collections.singletonList(chunkHolder.getPosition())), (old, nothing) -> {
                        old.add(chunkHolder.getPosition());
                        return old;
                    });
                } else {
                    tickets.forEach(ticket -> chunksByTicketName.merge(ticket.getType().toString(), new HashSet<>(Collections.singletonList(chunkHolder.getPosition())), (old, nothing) -> {
                        old.add(chunkHolder.getPosition());
                        return old;
                    }));
                }

            });
        }

    }

    public void reply(CommandSource source) {
        source.sendFeedback(new StringTextComponent("Total loaded Chunks: " + total), true);
        source.sendFeedback(new StringTextComponent("Loaded Chunks by Type: "), true);
        chunksByLocationType.forEach((x, y) -> {
            CommandUtils.sendCommandMessage(source, new StringTextComponent(x + " : " + y.size()), "/cu loadedChunks byLocation " + x, true);
        });
        source.sendFeedback(new StringTextComponent("Loaded Chunks by Ticket: "), true);
        chunksByTicketName.forEach((x, y) -> {
            CommandUtils.sendCommandMessage(source, new StringTextComponent(x + " : " + y.size()), "/cu loadedChunks byTicket " + x, true);
        });

    }

    public void replyWithLocation(CommandSource source, String word) throws CommandSyntaxException {
        source.sendFeedback(new StringTextComponent("Chunks with LocationType " + word), true);
        sendChunkPositions(source, chunksByLocationType.get(word));
    }

    private void sendChunkPositions(CommandSource source, Set<ChunkPos> chunks) throws CommandSyntaxException {
        for (ChunkPos chunkPos : chunks) {
            BlockPos pos = chunkPos.asBlockPos();
            CommandUtils.sendCommandMessage(source, new StringTextComponent(chunkPos.toString()), "/cu tp " + (source.getEntity() instanceof PlayerEntity ? source.asPlayer().getName().getString() : "Console") + " " + pos.getX() + " " + pos.getY() + " " + pos.getZ(), true);
        }
    }

    public void replyWithTicket(CommandSource source, String word) throws CommandSyntaxException {
        Set<ChunkPos> chunks = chunksByTicketName.get(word);
        source.sendFeedback(new StringTextComponent("Chunks with " + word + " Ticket"), true);
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


    public void applyFilter(String chunkDataFilter) {
        filter = chunkDataFilter;
    }
}
