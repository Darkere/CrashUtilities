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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.concurrent.atomic.AtomicReference;

public class UnstuckCommand implements Command<CommandSourceStack> {
    private static final UnstuckCommand cmd = new UnstuckCommand();

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("unstuck")
                .then(Commands.argument("name", StringArgumentType.string())
                        .suggests(CommandUtils.PROFILEPROVIDER)
                        .executes(cmd));

    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        ServerPlayer player = context.getSource().getServer().getPlayerList().getPlayerByName(name);
        AtomicReference<Boolean> success = new AtomicReference<>();
        if (player == null) {
            success.set(WorldUtils.applyToPlayer(name, context.getSource().getServer(), (fakePlayer) -> {
                ServerLevel overworld = context.getSource().getServer().getLevel(Level.OVERWORLD);
                BlockPos spawn = overworld.getSharedSpawnPos();
                fakePlayer.setLevel(overworld);
                fakePlayer.setPos(spawn.getX(), spawn.getY(), spawn.getZ());
            }));

        } else {
            BlockPos p = context.getSource().getServer().getLevel(Level.OVERWORLD).getSharedSpawnPos();
            WorldUtils.teleportPlayer(player, player.getLevel(), player.getServer().getLevel(Level.OVERWORLD), p);
        }

        if (success.get()) {
            context.getSource().sendSuccess(new TextComponent("Sent Player " + name + " to Spawn"), true);
        } else {
            context.getSource().sendSuccess(new TextComponent("Unable to load playerdata for " + name), true);
        }

        return 0;
    }

}
