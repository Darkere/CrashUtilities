package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.WorldUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.registries.ForgeRegistries;

public class UnstuckCommand implements Command<CommandSource> {
    private static final UnstuckCommand cmd = new UnstuckCommand();

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("unstuck")
            .then(Commands.argument("name", StringArgumentType.string())
            .executes(cmd));

    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context,"name");
        ServerPlayerEntity player = context.getSource().getServer().getPlayerList().getPlayerByUsername(name);
        if(player == null){
            WorldUtils.teleportOfflinePlayer(name,context.getSource());
        } else {
            BlockPos p = context.getSource().getServer().getWorld(DimensionType.OVERWORLD).getSpawnPoint();
            context.getSource().getServer().getCommandManager().handleCommand(context.getSource(),"cu tp "+ name + " " + p.getX() + " " + p.getY() + " " + p.getZ() + " minecraft:overworld");
        }
        return 0;
    }

}
