package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.WorldUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class RemoveEntityMessage {
    ResourceKey<Level> worldRegistryKey;
    UUID id;
    boolean tile;
    boolean force;

    public RemoveEntityMessage(ResourceKey<Level> worldRegistryKey, UUID id, boolean tile, boolean force) {
        this.worldRegistryKey = worldRegistryKey;
        this.id = id;
        this.tile = tile;
        this.force = force;
    }

    public static void encode(RemoveEntityMessage data, FriendlyByteBuf buf) {
        NetworkTools.writeWorldKey(data.worldRegistryKey, buf);
        buf.writeUUID(data.id);
        buf.writeBoolean(data.tile);
        buf.writeBoolean(data.force);
    }


    public static RemoveEntityMessage decode(FriendlyByteBuf buf) {
        return new RemoveEntityMessage(NetworkTools.readWorldKey(buf), buf.readUUID(), buf.readBoolean(), buf.readBoolean());
    }

    public static boolean handle(RemoveEntityMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || !player.hasPermissions(CommandUtils.PERMISSION_LEVEL)) return;
            if (data.tile) {
                WorldUtils.removeTileEntity(player.getServer().getLevel(data.worldRegistryKey), data.id, data.force);
            } else {
                WorldUtils.removeEntity(player.getServer().getLevel(data.worldRegistryKey), data.id);
            }
        });
        return true;
    }
}
