package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.Screens.PlayerInvContainer;
import com.darkere.crashutils.WorldUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public record PlayerInventoryRequestMessage(String playerName) implements CustomPacketPayload {
    public static final Type<PlayerInventoryRequestMessage> TYPE = new Type<>(CrashUtils.Identifier( "playerinventoryrequestmessage"));
    public static final StreamCodec<? super RegistryFriendlyByteBuf, PlayerInventoryRequestMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,PlayerInventoryRequestMessage::playerName,
            PlayerInventoryRequestMessage::new
    );

    public static boolean handle(PlayerInventoryRequestMessage data, IPayloadContext ctx) {
            ServerPlayer player = (ServerPlayer)ctx.player();
            MinecraftServer server = player.level().getServer();
            if(!CrashUtils.HasCUPerms(player))
                return true;
            Player otherPlayer = player.level().getServer().getPlayerList().getPlayerByName(data.playerName);
            if (otherPlayer == null)
                otherPlayer = WorldUtils.loadFakePlayer(data.playerName,player.level().getServer(),player);

            Map<String, Integer> curios = new LinkedHashMap<>();
            if (CrashUtils.curiosLoaded) {
                CuriosApi.getCuriosInventory(otherPlayer).get().getCurios().forEach((s, handler) -> {
                    curios.put(s, handler.getSlots());
                });
            }

            player.doCloseContainer();
            player.nextContainerCounter();
            int id = player.containerCounter;

            Network.sendToPlayer(player, new OpenPlayerInvMessage(curios, data.playerName, id));
            player.containerMenu = new PlayerInvContainer(player, otherPlayer, id, null, null, 0);
            player.initMenu(player.containerMenu);


        return true;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
