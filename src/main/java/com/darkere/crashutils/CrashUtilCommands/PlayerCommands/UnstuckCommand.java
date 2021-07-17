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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.concurrent.atomic.AtomicReference;

public class UnstuckCommand implements Command<CommandSource> {
    private static final UnstuckCommand cmd = new UnstuckCommand();

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("unstuck")
                .then(Commands.argument("name", StringArgumentType.string())
                        .suggests(CommandUtils.PROFILEPROVIDER)
                        .executes(cmd));

    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        ServerPlayerEntity player = context.getSource().getServer().getPlayerList().getPlayerByName(name);
        AtomicReference<Boolean> success = new AtomicReference<>();
        if (player == null) {
            success.set(WorldUtils.applyToPlayer(name, context.getSource().getServer(), (fakePlayer) -> {
                ServerWorld overworld = context.getSource().getServer().getLevel(World.OVERWORLD);
                BlockPos spawn = overworld.getSharedSpawnPos();
                fakePlayer.setLevel(overworld);
                fakePlayer.setPos(spawn.getX(), spawn.getY(), spawn.getZ());
            }));

        } else {
            BlockPos p = context.getSource().getServer().getLevel(World.OVERWORLD).getSharedSpawnPos();
            WorldUtils.teleportPlayer(player, player.getLevel(), player.getServer().getLevel(World.OVERWORLD), p);
        }

        if (success.get()) {
            context.getSource().sendSuccess(new StringTextComponent("Sent Player " + name + " to Spawn"), true);
        } else {
            context.getSource().sendSuccess(new StringTextComponent("Unable to load playerdata for " + name), true);
        }

        return 0;
    }

}
