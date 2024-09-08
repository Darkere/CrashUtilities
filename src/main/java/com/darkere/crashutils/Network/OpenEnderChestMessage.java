package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nullable;
import java.util.Optional;

public record OpenEnderChestMessage(String playerName) implements CustomPacketPayload {

    public static final Type<OpenEnderChestMessage> TYPE = new Type<>(CrashUtils.ResourceLocation("openenderchestmessage"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenEnderChestMessage> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.STRING_UTF8,OpenEnderChestMessage::playerName,OpenEnderChestMessage::new);


    public static boolean handle(OpenEnderChestMessage data, IPayloadContext ctx) {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if(player.getServer() == null || !player.hasPermissions(CommandUtils.PERMISSION_LEVEL)){
                return true;
            }
            Player otherPlayer = player.getServer().getPlayerList().getPlayerByName(data.playerName);
            if (otherPlayer == null) {
                Optional<GameProfile> profile = player.getServer().getProfileCache().get(data.playerName);
                if (profile.isEmpty()) {
                    CommandUtils.sendMessageToPlayer(player,"Cannot find Player" );
                    return true;
                }
                otherPlayer = new FakePlayer(player.getServer().getLevel(Level.OVERWORLD), profile.get());
                Optional<CompoundTag> nbt = player.getServer().playerDataStorage.load(otherPlayer);
                if (nbt.isEmpty()) {
                    CommandUtils.sendMessageToPlayer(player,"Cannot load playerData");
                    return true;
                }
                otherPlayer.load(nbt.get());
            }

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

                            player.getServer().playerDataStorage.save(finalOtherPlayer);
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
