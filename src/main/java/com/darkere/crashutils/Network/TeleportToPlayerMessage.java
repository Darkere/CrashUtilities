package com.darkere.crashutils.Network;

import com.darkere.crashutils.WorldUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class TeleportToPlayerMessage {
    String name;

    public TeleportToPlayerMessage(String name) {
        this.name = name;
    }

    public static void encode(TeleportToPlayerMessage data, PacketBuffer buf) {
        buf.writeString(data.name);
    }


    public static TeleportToPlayerMessage decode(PacketBuffer buf) {
        return new TeleportToPlayerMessage(buf.readString(100));
    }

    public static void handle(TeleportToPlayerMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) return;
            if (!player.hasPermissionLevel(4)) return;
            World ori = player.getEntityWorld();
            AtomicReference<World> dest = new AtomicReference<>();
            AtomicReference<BlockPos> otherPos = new AtomicReference<>();
            WorldUtils.applyToPlayer(data.name, player.server, o -> {
                dest.set(o.getEntityWorld());
                otherPos.set(o.getPosition());
            });
            if (otherPos.get() == null) {
                player.sendMessage(new StringTextComponent("Failed to load Player"), new UUID(0, 0));
            }
            WorldUtils.teleportPlayer(player, ori, dest.get(), otherPos.get());
        });
        ctx.get().setPacketHandled(true);
    }
}
