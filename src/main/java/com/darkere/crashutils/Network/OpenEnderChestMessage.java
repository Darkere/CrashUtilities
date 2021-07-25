package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

public class OpenEnderChestMessage {
    String playerName;
    public OpenEnderChestMessage(String playerName) {
        this.playerName = playerName;
    }


    public static void encode(OpenEnderChestMessage data, PacketBuffer buf) {
        buf.writeUtf(data.playerName);
    }


    public static OpenEnderChestMessage decode(PacketBuffer buf) {
        return new OpenEnderChestMessage(buf.readUtf(32000));
    }

    public static boolean handle(OpenEnderChestMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if(player == null|| player.getServer() == null || !player.hasPermissions(CommandUtils.PERMISSION_LEVEL)){
                return;
            }
            PlayerEntity otherPlayer = player.getServer().getPlayerList().getPlayerByName(data.playerName);
            if (otherPlayer == null) {
                GameProfile profile = player.getServer().getProfileCache().get(data.playerName);
                if (profile == null) {
                    player.sendMessage(new StringTextComponent("Cannot find Player"), new UUID(0, 0));
                    return;
                }
                otherPlayer = new FakePlayer(player.getServer().getLevel(World.OVERWORLD), profile);
                CompoundNBT nbt = player.getServer().playerDataStorage.load(otherPlayer);
                if (nbt == null) {
                    player.sendMessage(new StringTextComponent("Cannot load playerData"), new UUID(0, 0));
                    return;
                }
                otherPlayer.load(nbt);
            }

            PlayerEntity finalOtherPlayer = otherPlayer;

            NetworkHooks.openGui(player, new INamedContainerProvider() {
                @Override
                public ITextComponent getDisplayName() {
                    return finalOtherPlayer.getDisplayName();
                }

                @Nullable
                @Override
                public Container createMenu(int id, PlayerInventory p_createMenu_2_, PlayerEntity p_createMenu_3_) {
                    return new ChestContainer(ContainerType.GENERIC_9x3, id, player.inventory, finalOtherPlayer.getEnderChestInventory(), 3){
                        @Override
                        public void removed(PlayerEntity p_75134_1_) {
                            super.removed(p_75134_1_);

                            player.getServer().playerDataStorage.save(finalOtherPlayer);
                        }

                        @Override
                        public boolean stillValid(PlayerEntity p_75145_1_) {
                            return true;
                        }
                    };
                }
            });
        });
        return true;
    }
}
