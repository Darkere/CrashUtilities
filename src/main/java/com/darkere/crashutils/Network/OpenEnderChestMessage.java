package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public class OpenEnderChestMessage {
    String playerName;
    public OpenEnderChestMessage(String playerName) {
        this.playerName = playerName;
    }


    public static void encode(OpenEnderChestMessage data, FriendlyByteBuf buf) {
        buf.writeUtf(data.playerName);
    }


    public static OpenEnderChestMessage decode(FriendlyByteBuf buf) {
        return new OpenEnderChestMessage(buf.readUtf(32000));
    }

    public static boolean handle(OpenEnderChestMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if(player == null|| player.getServer() == null || !player.hasPermissions(CommandUtils.PERMISSION_LEVEL)){
                return;
            }
            Player otherPlayer = player.getServer().getPlayerList().getPlayerByName(data.playerName);
            if (otherPlayer == null) {
                Optional<GameProfile> profile = player.getServer().getProfileCache().get(data.playerName);
                if (profile.isEmpty()) {
                    CommandUtils.sendMessageToPlayer(player,"Cannot find Player" );
                    return;
                }
                otherPlayer = new FakePlayer(player.getServer().getLevel(Level.OVERWORLD), profile.get());
                CompoundTag nbt = player.getServer().playerDataStorage.load(otherPlayer);
                if (nbt == null) {
                    CommandUtils.sendMessageToPlayer(player,"Cannot load playerData");
                    return;
                }
                otherPlayer.load(nbt);
            }

            Player finalOtherPlayer = otherPlayer;

            NetworkHooks.openGui(player, new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return finalOtherPlayer.getDisplayName();
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int id, Inventory p_createMenu_2_, Player p_createMenu_3_) {
                    return new ChestMenu(MenuType.GENERIC_9x3, id, player.getInventory(), finalOtherPlayer.getEnderChestInventory(), 3){
                        @Override
                        public void removed(Player p_75134_1_) {
                            super.removed(p_75134_1_);

                            player.getServer().playerDataStorage.save(finalOtherPlayer);
                        }

                        @Override
                        public boolean stillValid(Player p_75145_1_) {
                            return true;
                        }
                    };
                }
            });
        });
        return true;
    }
}
