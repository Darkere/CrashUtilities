package com.darkere.crashutils.CrashUtilCommands.InventoryCommands;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.Network.Network;
import com.darkere.crashutils.Network.OpenPlayerInvMessage;
import com.darkere.crashutils.WorldUtils;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.network.FMLConnectionData;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class InventoryOpenCommand {


    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("open")
                .then(Commands.argument("player",StringArgumentType.string())
                        .suggests(CommandUtils.PROFILEPROVIDER)
                        .executes(ctx->openInventory(StringArgumentType.getString(ctx,"player"),ctx)));
    }

    private static int openInventory(String player, CommandContext<CommandSource> ctx) {
        if(!(ctx.getSource().getEntity() instanceof ServerPlayerEntity)){
            ctx.getSource().sendFailure(new StringTextComponent("You need to be a player to use this command, consider using \"cu inventory read\" instead"));
            return 0;
        }
        ServerPlayerEntity sourcePlayer = (ServerPlayerEntity) ctx.getSource().getEntity();
        ServerPlayerEntity otherPlayer = ctx.getSource().getServer().getPlayerList().getPlayerByName(player);
        if(otherPlayer == null){
            if(!WorldUtils.applyToPlayer(player, ctx.getSource().getServer(),fakeplayer->{})){
                ctx.getSource().sendFailure(new StringTextComponent("Unable to find playerdata for " + player));
                return 0;
            }
        }
        FMLConnectionData data = NetworkHooks.getConnectionData(sourcePlayer.connection.getConnection());
        if(data != null){
            if(data.getModList().contains(CrashUtils.MODID)){
                Network.sendToPlayer(new OpenPlayerInvMessage(sourcePlayer.containerCounter,player,));
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
