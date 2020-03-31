package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.WorldUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoadedChunksOverviewCommand implements Command<CommandSource> {
    private static final LoadedChunksOverviewCommand cmd = new LoadedChunksOverviewCommand();

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("loadedChunksOverview")
            .then(Commands.argument("dim", DimensionArgument.getDimension()).executes(cmd))
            .executes(cmd);

    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        List<ServerWorld> worlds = WorldUtils.getWorldsFromDimensionArgument(context);
        List<TileEntity> tileEntities = new ArrayList<>();
        worlds.forEach(x->tileEntities.addAll(x.loadedTileEntityList));
        Map<ChunkPos,List<TileEntity>> list ;
        for (TileEntity tileEntity : tileEntities) {


        }
        return 0;
    }
    //
//    @Override
//    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
//        List<World> worlds = WorldUtils.getWorldsFromDimensionArgument(context);
//        int total = 0;
//        AtomicInteger forced = new AtomicInteger();
//        LoadedChunkList list = new LoadedChunkList();
//        AtomicInteger playerTracking = new AtomicInteger();
//        AtomicInteger start = new AtomicInteger();
//        AtomicInteger playerTicket = new AtomicInteger();
//        for (World world : worlds) {
//            ChunkManager chunkManager = ((ServerChunkProvider) world.getChunkProvider()).chunkManager;
//            TicketManager ticketManager = chunkManager.getTicketManager();
//            total += chunkManager.getLoadedChunkCount();
//            Iterable<ChunkHolder> chunkHolders = chunkManager.getLoadedChunksIterable();
//            chunkHolders.forEach(x -> {
//                Stream<ServerPlayerEntity> playerStream = chunkManager.getTrackingPlayers(x.getPosition(), false);
//                List<ServerPlayerEntity> playerEntities = playerStream.collect(Collectors.toList());
//                if(!playerEntities.isEmpty()){
//                    playerTracking.addAndGet(1);
//                    playerEntities.forEach(list::addPlayerChunk);
//                } else {
//                        Map<ChunkPos, Set<String>> chunksWithTicket = new HashMap<>();
//                        AtomicBoolean found = new AtomicBoolean();
//                        ticketManager.getTicketSet(x.getPosition().asLong()).forEach(y -> {
//                            Set<String> tickets = chunksWithTicket.getOrDefault(x.getPosition(), new HashSet<>());
//                            tickets.add(y.getType().toString());
//                            chunksWithTicket.put(x.getPosition(), tickets);
//                            found.set(true);
//                        });
//                        if(!found.get()){
//                            list.addChunk(x.getPosition(),"No Ticket");
//                        }
//                        chunksWithTicket.forEach((k,v) ->{
//                            if(v.contains("forced")){
//                                forced.addAndGet(1);
//                                list.addChunk(k,"forced");
//                            } else if(v.contains("start")){
//                                start.addAndGet(1);
//                                list.addChunk(k,"start");
//                            } else if(v.contains("player")){
//                                playerTicket.addAndGet(1);
//                                list.addChunk(k,"player");
//                            } else {
//                                list.addChunk(k,"Other");
//                            }
//                        });
//
//                }
//            });
//        }
//        String s = "Total: " + total + " PlayerTracking: " + playerTracking + "Tickets: Forced: " + forced + "player"+ playerTicket + " Start: " + start;
//        CommandUtils.sendNormalMessage(context.getSource(), s, TextFormatting.WHITE);
//        list.reply(context.getSource());
//        return Command.SINGLE_SUCCESS;
//    }
}
