package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.Screens.PlayerInvContainer;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class PlayerInventoryRequestMessage {
    String playerName;

    public PlayerInventoryRequestMessage(String s) {
        playerName = s;
    }

    public static void encode(PlayerInventoryRequestMessage data, FriendlyByteBuf buf) {
        buf.writeInt(data.playerName.length());
        buf.writeUtf(data.playerName);

    }

    public static PlayerInventoryRequestMessage decode(FriendlyByteBuf buf) {
        return new PlayerInventoryRequestMessage(buf.readUtf(buf.readInt()));
    }

    public static boolean handle(PlayerInventoryRequestMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            MinecraftServer server = player.getServer();
            if(!player.hasPermissions(CommandUtils.PERMISSION_LEVEL)) return;
            Player otherPlayer = ctx.get().getSender().getServer().getPlayerList().getPlayerByName(data.playerName);
            if (otherPlayer == null) {
                Optional<GameProfile> profile = server.getProfileCache().get(data.playerName);
                if (profile.isEmpty()) {
                    player.sendMessage(new TextComponent("Cannot find Player"), new UUID(0, 0));
                    return;
                }
                otherPlayer = new FakePlayer(server.getLevel(Level.OVERWORLD), profile.get());
                CompoundTag nbt = server.playerDataStorage.load(otherPlayer);
                if (nbt == null) {
                    player.sendMessage(new TextComponent("Cannot load playerData"), new UUID(0, 0));
                    return;
                }
                otherPlayer.load(nbt);
            }

            Map<String, Integer> curios = new LinkedHashMap<>();
            if (CrashUtils.curiosLoaded) {
                CuriosApi.getCuriosHelper().getCuriosHandler(otherPlayer).orElse(null).getCurios().forEach((s, handler) -> {
                    curios.put(s, handler.getSlots());
                });
            }

            player.doCloseContainer();
            player.nextContainerCounter();
            int id = player.containerCounter;

            Network.sendToPlayer(player, new OpenPlayerInvMessage(id, data.playerName, curios));
            player.containerMenu = new PlayerInvContainer(player, otherPlayer, id, null, null, 0);
            player.initMenu(player.containerMenu);


        });
        return true;
    }
}
