package com.darkere.crashutils.CrashUtilCommands.InventoryCommands;

import com.darkere.crashutils.CommandUtils;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class InventoryOpenEnderChestCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("enderchest")
            .then(Commands.argument("player", StringArgumentType.string())
                .suggests(CommandUtils.PROFILEPROVIDER)
                .executes(ctx -> openInventory(StringArgumentType.getString(ctx, "player"), ctx)));
    }

    private static int openInventory(String playerName, CommandContext<CommandSource> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayerEntity)) {
            ctx.getSource().sendFailure(new StringTextComponent("You need to be a player to use this command, consider using \"cu inventory read\" instead"));
            return 0;
        }
        ServerPlayerEntity sourcePlayer = (ServerPlayerEntity) ctx.getSource().getEntity();
        PlayerEntity otherPlayer = ctx.getSource().getServer().getPlayerList().getPlayerByName(playerName);
        if (otherPlayer == null) {
            GameProfile profile = ctx.getSource().getServer().getProfileCache().get(playerName);
            if (profile == null) {
                sourcePlayer.sendMessage(new StringTextComponent("Cannot find Player"), new UUID(0, 0));
                return 0;
            }
            otherPlayer = new FakePlayer(ctx.getSource().getServer().getLevel(World.OVERWORLD), profile);
            CompoundNBT nbt = ctx.getSource().getServer().playerDataStorage.load(otherPlayer);
            if (nbt == null) {
                sourcePlayer.sendMessage(new StringTextComponent("Cannot load playerData"), new UUID(0, 0));
                return 0;
            }
            otherPlayer.load(nbt);
        }

        PlayerEntity finalOtherPlayer = otherPlayer;

        NetworkHooks.openGui(sourcePlayer, new INamedContainerProvider() {
            @Override
            public ITextComponent getDisplayName() {
                return finalOtherPlayer.getDisplayName();
            }

            @Nullable
            @Override
            public Container createMenu(int id, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
                return new ChestContainer(ContainerType.GENERIC_9x3, id, sourcePlayer.inventory, finalOtherPlayer.getEnderChestInventory(), 3){
                    @Override
                    public void removed(PlayerEntity p_75134_1_) {
                        super.removed(p_75134_1_);

                        ctx.getSource().getServer().playerDataStorage.save(finalOtherPlayer);
                    }

                    @Override
                    public boolean stillValid(PlayerEntity p_75145_1_) {
                        return true;
                    }
                };
            }
        });
        return Command.SINGLE_SUCCESS;
    }
}
