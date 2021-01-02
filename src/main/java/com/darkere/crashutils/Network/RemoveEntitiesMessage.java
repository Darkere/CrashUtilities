package com.darkere.crashutils.Network;

import com.darkere.crashutils.WorldUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class RemoveEntitiesMessage {
    RegistryKey<World> worldRegistryKey;
    ResourceLocation rl;
    ChunkPos pos;
    boolean tile;
    boolean force;

    public RemoveEntitiesMessage(RegistryKey<World> worldRegistryKey, ResourceLocation rl, ChunkPos pos, boolean tile, boolean force) {
        this.worldRegistryKey = worldRegistryKey;
        this.rl = rl;
        this.pos = pos;
        this.tile = tile;
        this.force = force;
    }


    public static void encode(RemoveEntitiesMessage data, PacketBuffer buf) {
        NetworkTools.writeWorldKey(data.worldRegistryKey, buf);
        buf.writeResourceLocation(data.rl);
        buf.writeBoolean(data.tile);
        buf.writeBoolean(data.force);
        if (data.pos == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeLong(data.pos.asLong());
        }
    }


    public static RemoveEntitiesMessage decode(PacketBuffer buf) {
        RegistryKey<World> world = NetworkTools.readWorldKey(buf);
        ResourceLocation rl = buf.readResourceLocation();
        boolean tile = buf.readBoolean();
        boolean force = buf.readBoolean();
        ChunkPos pos = null;
        if (buf.readBoolean()) {
            pos = new ChunkPos(buf.readLong());
        }
        return new RemoveEntitiesMessage(world, rl, pos, tile, force);
    }

    public static boolean handle(RemoveEntitiesMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player == null || !player.hasPermissionLevel(2)) return;
            World world = player.getServer().getWorld(data.worldRegistryKey);
            if (data.pos == null) {
                if (data.tile) {
                    WorldUtils.removeTileEntityType(world, data.rl, data.force);
                } else {
                    WorldUtils.removeEntityType(world, data.rl, data.force);
                }
            } else {
                if (data.tile) {
                    WorldUtils.removeTileEntitiesInChunk(world, data.pos, data.rl, data.force);
                } else {
                    WorldUtils.removeEntitiesInChunk(world, data.pos, data.rl, data.force);
                }
            }
        });
        return true;
    }
}
