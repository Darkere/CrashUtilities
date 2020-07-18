package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.WorldUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.concurrent.atomic.AtomicReference;

public class TeleportCommand implements Command<CommandSource> {
    private static final TeleportCommand cmd = new TeleportCommand();
    private static final SuggestionProvider<CommandSource> sugg = (ctx, builder) -> ISuggestionProvider.suggest(ctx.getSource().getServer().getPlayerProfileCache().gameProfiles.stream().map(GameProfile::getName), builder);
    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("tp")
            .then(Commands.argument("player", StringArgumentType.string())
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .executes(cmd)
                    .then(Commands.argument("dim", DimensionArgument.getDimension())
                        .executes(cmd)))
                .then(Commands.argument("name", StringArgumentType.string())
                    .suggests(sugg)
                        .executes(cmd)));


    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerWorld playerWorld = context.getSource().getWorld();
        ServerWorld destWorld = null;
        ServerPlayerEntity otherPlayer = null;
        ServerPlayerEntity player = null;
        BlockPos pos = null;
        try {
            destWorld = DimensionArgument.getDimensionArgument(context, "dim");
        } catch (IllegalArgumentException e) {
            //NOOP
        }
        try {
            otherPlayer = context.getSource().getServer().getPlayerList().getPlayerByUsername(StringArgumentType.getString(context,"name"));
        }catch (IllegalArgumentException e){

        }
        try {
            pos = BlockPosArgument.getBlockPos(context, "pos");
        }catch (IllegalArgumentException e){

        }
        if (destWorld == null) {
            destWorld = playerWorld;
        }
        try {
            player = context.getSource().getServer().getPlayerList().getPlayerByUsername(StringArgumentType.getString(context,"player"));
        } catch (IllegalArgumentException e){

        }
        if(player == null) return 0;
        if(pos == null){
            if(context.getSource().getServer().getPlayerList().getPlayers().contains(otherPlayer)){
              pos = new BlockPos(otherPlayer.getPositionVec());
            } else {
                AtomicReference<BlockPos> offlinePlayerPos = new AtomicReference<>();
                WorldUtils.applyToPlayer(otherPlayer.getName().getString(),context,fakePlayer->{
                    offlinePlayerPos.set(new BlockPos(fakePlayer.getPositionVec()));
                });
                pos = offlinePlayerPos.get();
            }
        }
        if (context.getSource().getServer().getPlayerList().getPlayers().contains(player)) {
            WorldUtils.teleportPlayer(player, playerWorld, destWorld, pos);
        } else {
            ServerWorld finalDestWorld = destWorld;
            BlockPos finalPos = pos;
            WorldUtils.applyToPlayer(player.getName().getString(), context, fakePlayer -> {
                fakePlayer.setPosition(finalPos.getX(), finalPos.getY(), finalPos.getZ());
                fakePlayer.setWorld(finalDestWorld);
            });
        }

        return Command.SINGLE_SUCCESS;
    }
}
