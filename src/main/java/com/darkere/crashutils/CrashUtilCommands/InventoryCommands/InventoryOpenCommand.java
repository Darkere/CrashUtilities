package com.darkere.crashutils.CrashUtilCommands.InventoryCommands;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.Network.Network;
import com.darkere.crashutils.Network.OpenPlayerInvMessage;
import com.darkere.crashutils.Screens.PlayerInvContainer;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.ConnectionData;
import net.minecraftforge.network.NetworkHooks;
import top.theillusivec4.curios.api.CuriosApi;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class InventoryOpenCommand {


    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("open")
            .then(Commands.argument("player", StringArgumentType.string())
                .suggests(CommandUtils.PROFILEPROVIDER)
                .executes(ctx -> openInventory(StringArgumentType.getString(ctx, "player"), ctx)));
    }

    private static int openInventory(String playerName, CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getEntity() instanceof ServerPlayer)) {
            ctx.getSource().sendFailure(CommandUtils.CreateTextComponent("You need to be a player to use this command, consider using \"cu inventory read\" instead"));
            return 0;
        }
        ServerPlayer sourcePlayer = (ServerPlayer) ctx.getSource().getEntity();
        Player otherPlayer = ctx.getSource().getServer().getPlayerList().getPlayerByName(playerName);
        if (otherPlayer == null) {
            Optional<GameProfile> profile = ctx.getSource().getServer().getProfileCache().get(playerName);
            if (profile.isEmpty()) {
                CommandUtils.sendMessageToPlayer(sourcePlayer, "Cannot find Player");
                return 0;
            }
            otherPlayer = new FakePlayer(ctx.getSource().getServer().getLevel(Level.OVERWORLD), profile.get());
            CompoundTag nbt = ctx.getSource().getServer().playerDataStorage.load(otherPlayer);
            if (nbt == null) {
                sourcePlayer.sendSystemMessage(CommandUtils.CreateTextComponent("Cannot load playerData"), ChatType.SYSTEM);
                return 0;
            }
            otherPlayer.load(nbt);
        }

        ConnectionData data = NetworkHooks.getConnectionData(sourcePlayer.connection.getConnection());
        if (data != null && data.getModList().contains(CrashUtils.MODID)) {
            sourcePlayer.doCloseContainer();
            sourcePlayer.nextContainerCounter();
            int id = sourcePlayer.containerCounter;

            Map<String, Integer> curios = new LinkedHashMap<>();
            if (CrashUtils.curiosLoaded) {
                CuriosApi.getCuriosHelper().getCuriosHandler(otherPlayer).orElse(null).getCurios().forEach((s, handler) -> {
                    curios.put(s, handler.getSlots());
                });
            }

            Network.sendToPlayer(sourcePlayer, new OpenPlayerInvMessage(id, otherPlayer.getName().getString(), curios));
            sourcePlayer.containerMenu = new PlayerInvContainer(sourcePlayer, otherPlayer, id, null, null, 0);
            sourcePlayer.initMenu(sourcePlayer.containerMenu);

            return Command.SINGLE_SUCCESS;
        }

        Player finalOtherPlayer = otherPlayer;
        NetworkHooks.openGui(sourcePlayer, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return finalOtherPlayer.getDisplayName();
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int id, Inventory p_createMenu_2_, Player p_createMenu_3_) {
                return new ChestMenu(MenuType.GENERIC_9x4, id, sourcePlayer.getInventory(), finalOtherPlayer.getInventory(), 4) {
                    @Override
                    public void removed(Player p_75134_1_) {
                        super.removed(p_75134_1_);

                        ctx.getSource().getServer().playerDataStorage.save(finalOtherPlayer);
                    }

                    @Override
                    public boolean stillValid(Player p_75145_1_) {
                        return true;
                    }
                };
            }
        });

        return Command.SINGLE_SUCCESS;
    }
}
