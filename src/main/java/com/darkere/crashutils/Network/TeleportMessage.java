package com.darkere.crashutils.Network;

import com.darkere.crashutils.WorldUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TeleportMessage {
    RegistryKey<World> origin;
    RegistryKey<World> dest;
    BlockPos pos;

    public TeleportMessage(RegistryKey<World> origin, RegistryKey<World> dest, BlockPos pos) {
        this.origin = origin;
        this.dest = dest;
        this.pos = pos;
    }

    public static void encode(TeleportMessage data, PacketBuffer buf) {
        NetworkTools.writeWorldKey(data.origin, buf);
        NetworkTools.writeWorldKey(data.dest, buf);
        buf.writeBlockPos(data.pos);
    }


    public static TeleportMessage decode(PacketBuffer buf) {
        return new TeleportMessage(
            NetworkTools.readWorldKey(buf),
            NetworkTools.readWorldKey(buf),
            buf.readBlockPos()
        );
    }

    public static void handle(TeleportMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null) return;
            if(!player.hasPermissionLevel(2)) return;
            ServerWorld ori = player.getServer().getWorld(data.origin);
            ServerWorld dest = player.getServer().getWorld(data.dest);
            WorldUtils.teleportPlayer(player, ori, dest, data.pos);
        });
        ctx.get().setPacketHandled(true);
    }
}
