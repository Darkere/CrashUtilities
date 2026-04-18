package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.WorldUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.util.Optional;

public record OpenEnderChestMessage(String playerName) implements CustomPacketPayload {

    public static final Type<OpenEnderChestMessage> TYPE = new Type<>(CrashUtils.Identifier("openenderchestmessage"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenEnderChestMessage> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8,OpenEnderChestMessage::playerName,OpenEnderChestMessage::new);


    public static boolean handle(OpenEnderChestMessage data, IPayloadContext ctx) {
            ServerPlayer player = (ServerPlayer) ctx.player();
            Permission.HasCommandLevel GameMasterPerms = new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS);
            if(ctx.player().level().getServer() == null || !player.permissions().hasPermission(GameMasterPerms)){
                return true;
            }
            Player otherPlayer = player.level().getServer().getPlayerList().getPlayerByName(data.playerName);
            if (otherPlayer == null)
                otherPlayer = WorldUtils.loadFakePlayer(data.playerName,player.level().getServer(),player);

            Player finalOtherPlayer = otherPlayer;

            player.openMenu( new MenuProvider() {
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

                            player.level().getServer().playerDataStorage.save(finalOtherPlayer);
                        }

                        @Override
                        public boolean stillValid(Player p_75145_1_) {
                            return true;
                        }
                    };
                }
            });
        return true;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
