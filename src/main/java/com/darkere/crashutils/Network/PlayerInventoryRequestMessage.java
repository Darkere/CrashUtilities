package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.Screens.PlayerInvContainer;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public record PlayerInventoryRequestMessage(String playerName) implements CustomPacketPayload {
    public static ResourceLocation ID = new ResourceLocation(CrashUtils.MODID, "playerinventoryrequestmessage");

    public static PlayerInventoryRequestMessage decode(FriendlyByteBuf buf) {
        return new PlayerInventoryRequestMessage(buf.readUtf(buf.readInt()));
    }

    public static boolean handle(PlayerInventoryRequestMessage data, PlayPayloadContext ctx) {
       ctx.workHandler().submitAsync(() -> {
            ServerPlayer player = (ServerPlayer)ctx.player().get();
            MinecraftServer server = player.getServer();
            if(!player.hasPermissions(CommandUtils.PERMISSION_LEVEL)) return;
            Player otherPlayer =player.getServer().getPlayerList().getPlayerByName(data.playerName);
            if (otherPlayer == null) {
                Optional<GameProfile> profile = server.getProfileCache().get(data.playerName);
                if (profile.isEmpty()) {
                    CommandUtils.sendMessageToPlayer(player,"Cannot find Player");
                    return;
                }
                otherPlayer = new FakePlayer(server.getLevel(Level.OVERWORLD), profile.get());
                CompoundTag nbt = server.playerDataStorage.load(otherPlayer);
                if (nbt == null) {
                    CommandUtils.sendMessageToPlayer(player,"Cannot load playerData");
                    return;
                }
                otherPlayer.load(nbt);
            }

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


        });
        return true;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(playerName.length());
        buf.writeUtf(playerName);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
