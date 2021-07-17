package com.darkere.crashutils.CrashUtilCommands.PlayerCommands;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.WorldUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.concurrent.atomic.AtomicReference;

public class TeleportCommand implements Command<CommandSource> {
    private static final TeleportCommand cmd = new TeleportCommand();

    public static ArgumentBuilder<CommandSource, ?> register() {
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
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerWorld playerWorld = context.getSource().getLevel();
        ServerWorld destWorld = null;
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
            pos = BlockPosArgument.getOrLoadBlockPos(context, "pos");
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
            context.getSource().sendFailure(new StringTextComponent("player to teleport not specified"));
            return 0;
        }

        //no position specified. Getting other players position
        ServerPlayerEntity otherPlayer = context.getSource().getServer().getPlayerList().getPlayerByName(otherPlayerName);
        if (pos == null && !otherPlayerName.isEmpty()) {
            if (otherPlayer != null) {
                pos = new BlockPos(otherPlayer.position());
            } else {
                AtomicReference<BlockPos> offlinePlayerPos = new AtomicReference<>();
                if (!WorldUtils.applyToPlayer(otherPlayerName, context.getSource().getServer(), fakePlayer -> {
                    offlinePlayerPos.set(new BlockPos(fakePlayer.position()));
                })) {
                    context.getSource().sendFailure(new StringTextComponent("Unable to load target players data"));
                    return 0;
                }
                pos = offlinePlayerPos.get();
            }
        }

        //determining source player and teleporting
        ServerPlayerEntity player = context.getSource().getServer().getPlayerList().getPlayerByName(playerName);
        if (player != null) {
            WorldUtils.teleportPlayer(player, playerWorld, destWorld, pos);
        } else {
            ServerWorld finalDestWorld = destWorld;
            BlockPos finalPos = pos;
            if (!WorldUtils.applyToPlayer(playerName, context.getSource().getServer(), fakePlayer -> {
                fakePlayer.setPos(finalPos.getX(), finalPos.getY(), finalPos.getZ());
                fakePlayer.setLevel(finalDestWorld);
            })) {
                context.getSource().sendFailure(new StringTextComponent("Unable to read source player data"));
                return 0;
            }
        }

        context.getSource().sendSuccess(new StringTextComponent("Teleported " + playerName + " to " + pos.toString()),true);

        return Command.SINGLE_SUCCESS;
    }
}
