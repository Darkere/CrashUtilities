package com.darkere.crashutils.CrashUtilCommands.PlayerCommands;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.WorldUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.atomic.AtomicReference;

public class TeleportCommand implements Command<CommandSourceStack> {
    private static final TeleportCommand cmd = new TeleportCommand();

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("tp")
                .then(Commands.argument("player", StringArgumentType.string())
                        .suggests(CommandUtils.PROFILEPROVIDER)
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(cmd)
                                .then(Commands.argument("dim", DimensionArgument.dimension())
                                        .executes(cmd)))
                        .then(Commands.argument("otherPlayer", StringArgumentType.string())
                                .suggests(CommandUtils.PROFILEPROVIDER)
                                .executes(cmd)));


    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerLevel playerWorld = context.getSource().getLevel();
        ServerLevel destWorld = null;
        String otherPlayerName = "";
        String playerName = "";
        BlockPos pos = null;
        try {
            destWorld = DimensionArgument.getDimension(context, "dim");
        } catch (IllegalArgumentException e) {
            //NOOP
        }

        try {
            otherPlayerName = StringArgumentType.getString(context, "otherPlayer");
        } catch (IllegalArgumentException e) {

        }

        try {
            pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
        } catch (IllegalArgumentException e) {

        }

        if (destWorld == null) {
            destWorld = playerWorld;
        }
        try {
            playerName = StringArgumentType.getString(context, "player");
        } catch (IllegalArgumentException e) {

        }
        if (playerName == null) {
            context.getSource().sendFailure(CommandUtils.CreateTextComponent("player to teleport not specified"));
            return 0;
        }

        //no position specified. Getting other players position
        ServerPlayer otherPlayer = context.getSource().getServer().getPlayerList().getPlayerByName(otherPlayerName);
        if (pos == null && !otherPlayerName.isEmpty()) {
            if (otherPlayer != null) {
                pos = otherPlayer.getOnPos();
            } else {
                AtomicReference<BlockPos> offlinePlayerPos = new AtomicReference<>();
                if (!WorldUtils.applyToPlayer(otherPlayerName, context.getSource().getServer(), fakePlayer -> {
                    offlinePlayerPos.set(fakePlayer.getOnPos());
                })) {
                    context.getSource().sendFailure(CommandUtils.CreateTextComponent("Unable to load target players data"));
                    return 0;
                }
                pos = offlinePlayerPos.get();
            }
        }

        //determining source player and teleporting
        ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayerByName(playerName);
        if (player != null) {
            WorldUtils.teleportPlayer(player, playerWorld, destWorld, pos);
        } else {
            ServerLevel finalDestWorld = destWorld;
            BlockPos finalPos = pos;
            if (!WorldUtils.applyToPlayer(playerName, context.getSource().getServer(), fakePlayer -> {
                fakePlayer.setPos(finalPos.getX(), finalPos.getY(), finalPos.getZ());
                fakePlayer.setLevel(finalDestWorld);
            })) {
                context.getSource().sendFailure(CommandUtils.CreateTextComponent("Unable to read source player data"));
                return 0;
            }
        }

        String finalPlayerName = playerName;
        BlockPos finalPos1 = pos;
        context.getSource().sendSuccess(()->CommandUtils.CreateTextComponent("Teleported " + finalPlayerName + " to " + finalPos1.toString()),true);

        return Command.SINGLE_SUCCESS;
    }
}
