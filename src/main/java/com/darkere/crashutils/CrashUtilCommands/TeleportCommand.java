package com.darkere.crashutils.CrashUtilCommands;

import com.darkere.crashutils.EntityList;
import com.darkere.crashutils.WorldUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class TeleportCommand implements Command<CommandSource> {
    private static final TeleportCommand cmd = new TeleportCommand();

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("tp")
            .then(Commands.argument("player", EntityArgument.player())
            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(cmd)
            .then(Commands.argument("dim", DimensionArgument.getDimension())
                .executes(cmd))));


    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        DimensionType worldType = context.getSource().getWorld().getDimension().getType();
        DimensionType destType = worldType;
        try {
            destType = DimensionArgument.getDimensionArgument(context, "dim");
        } catch (IllegalArgumentException e) {
            //NOOP
        }

        ServerPlayerEntity player = EntityArgument.getPlayer(context, "player");
        WorldUtils.teleportPlayer(player, worldType, destType, BlockPosArgument.getBlockPos(context, "pos"));
        return Command.SINGLE_SUCCESS;
    }
}
