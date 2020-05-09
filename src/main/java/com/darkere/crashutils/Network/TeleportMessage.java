package com.darkere.crashutils.Network;

import com.darkere.crashutils.WorldUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class TeleportMessage {
    DimensionType origin;
    DimensionType dest;
    BlockPos pos;

    public TeleportMessage(DimensionType origin, DimensionType dest, BlockPos pos) {
        this.origin = origin;
        this.dest = dest;
        this.pos = pos;
    }

    public static void encode(TeleportMessage data, PacketBuffer buf) {
        ResourceLocation desti = data.dest.getRegistryName();
        ResourceLocation ori = data.origin.getRegistryName();
        if(desti == null)desti = new ResourceLocation("minecraft:overworld");
        if(ori == null)ori = new ResourceLocation("minecraft:overworld");
        buf.writeResourceLocation(ori);
        buf.writeResourceLocation(desti);
        buf.writeBlockPos(data.pos);
    }


    public static TeleportMessage decode(PacketBuffer buf) {
        return new TeleportMessage(
            DimensionType.byName(buf.readResourceLocation()),
            DimensionType.byName(buf.readResourceLocation()),
            buf.readBlockPos()
        );
    }

    public static void handle(TeleportMessage data, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if(player == null)return;
            WorldUtils.teleportPlayer(player,data.origin,data.dest,data.pos);
        });
        ctx.get().setPacketHandled(true);
    }
}
