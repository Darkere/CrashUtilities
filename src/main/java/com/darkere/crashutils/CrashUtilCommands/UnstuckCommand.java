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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class UnstuckCommand implements Command<CommandSource> {
    private static final UnstuckCommand cmd = new UnstuckCommand();
    private static final SuggestionProvider<CommandSource> sugg = (ctx, builder) -> ISuggestionProvider.suggest(ctx.getSource().getServer().getPlayerProfileCache().gameProfiles.stream().map(GameProfile::getName), builder);

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("unstuck")
            .then(Commands.argument("name", StringArgumentType.string())
                .suggests(sugg)
                .executes(cmd));

    }

    @Override
    public int run(CommandContext<CommandSource> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "name");
        ServerPlayerEntity player = context.getSource().getServer().getPlayerList().getPlayerByUsername(name);
        if (player == null) {
            WorldUtils.applyToPlayer(name, context, (fakePlayer) -> {
                ServerWorld overworld = context.getSource().getServer().getWorld(World.field_234918_g_);
                BlockPos spawn = overworld.func_241135_u_();
                fakePlayer.setWorld(overworld);
                fakePlayer.setPosition(spawn.getX(),spawn.getY(),spawn.getZ());
            });
        } else {
            BlockPos p = context.getSource().getServer().getWorld(World.field_234918_g_).func_241135_u_();
            WorldUtils.teleportPlayer(player,player.getServerWorld(),player.getServer().getWorld(World.field_234918_g_),p);
        }
        context.getSource().sendFeedback(new StringTextComponent("Sent Player " + name + " to Spawn"), true);
        return 0;
    }

}
