package com.darkere.crashutils.Network;

import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.Screens.PlayerInvContainer;
import com.darkere.crashutils.WorldUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkHooks;
import top.theillusivec4.curios.api.CuriosAPI;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class PlayerInventoryRequestMessage {
    String playerName;

    public PlayerInventoryRequestMessage(String s) {
        playerName = s;
    }

    public static void encode(PlayerInventoryRequestMessage data, PacketBuffer buf) {
        buf.writeInt(data.playerName.length());
        buf.writeString(data.playerName);

    }

    public static PlayerInventoryRequestMessage decode(PacketBuffer buf) {
        return new PlayerInventoryRequestMessage(buf.readString(buf.readInt()));
    }

    public static boolean handle(PlayerInventoryRequestMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            MinecraftServer server = player.getServer();
            PlayerEntity otherPlayer = ctx.get().getSender().getServer().getPlayerList().getPlayerByUsername(data.playerName);
            if(otherPlayer == null){
                GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(data.playerName);
                ServerWorld overworld = server.getWorld(DimensionType.OVERWORLD);
                otherPlayer = FakePlayerFactory.get(overworld,profile);
                overworld.getSaveHandler().readPlayerData(otherPlayer);
            }

            WorldUtils.addPlayerContainerRel(player,otherPlayer);
            Map<String,Integer> curios = new LinkedHashMap<>();
            if(CrashUtils.curiosLoaded){
                CuriosAPI.getCuriosHandler(otherPlayer).orElse(null).getCurioMap().forEach((s,handler)->{
                    curios.put(s,handler.getSlots());
                });
            }
            NetworkHooks.openGui(player, new PlayerInvContainer.ContainerProvider(),buf->{
                buf.writeString(data.playerName);
                buf.writeInt(curios.size());
                curios.forEach((s,i)->{
                    buf.writeString(s);
                    buf.writeInt(i);
                });
            });
        });
        return true;
    }
}
