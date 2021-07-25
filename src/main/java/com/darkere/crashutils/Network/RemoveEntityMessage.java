package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.WorldUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class RemoveEntityMessage {
    RegistryKey<World> worldRegistryKey;
    UUID id;
    boolean tile;
    boolean force;

    public RemoveEntityMessage(RegistryKey<World> worldRegistryKey, UUID id, boolean tile, boolean force) {
        this.worldRegistryKey = worldRegistryKey;
        this.id = id;
        this.tile = tile;
        this.force = force;
    }

    public static void encode(RemoveEntityMessage data, PacketBuffer buf) {
        NetworkTools.writeWorldKey(data.worldRegistryKey, buf);
        buf.writeUUID(data.id);
        buf.writeBoolean(data.tile);
        buf.writeBoolean(data.force);
    }


    public static RemoveEntityMessage decode(PacketBuffer buf) {
        return new RemoveEntityMessage(NetworkTools.readWorldKey(buf), buf.readUUID(), buf.readBoolean(), buf.readBoolean());
    }

    public static boolean handle(RemoveEntityMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null || !player.hasPermissions(CommandUtils.PERMISSION_LEVEL)) return;
            if (data.tile) {
                WorldUtils.removeTileEntity(player.getServer().getLevel(data.worldRegistryKey), data.id, data.force);
            } else {
                WorldUtils.removeEntity(player.getServer().getLevel(data.worldRegistryKey), data.id, data.force);
            }
        });
        return true;
    }
}
