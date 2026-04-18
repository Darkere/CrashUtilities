package com.darkere.crashutils.Network;

import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.WorldUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RemoveEntitiesMessage(ResourceKey<Level> worldRegistryKey, Identifier rl, ChunkPos pos,
                                    boolean tile, boolean force, boolean All) implements CustomPacketPayload {
    public static final Type<RemoveEntitiesMessage> TYPE = new Type<>(CrashUtils.Identifier("removeentitiesmessage"));
    public static final StreamCodec<? super RegistryFriendlyByteBuf, RemoveEntitiesMessage> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), RemoveEntitiesMessage::worldRegistryKey,
            Identifier.STREAM_CODEC, RemoveEntitiesMessage::rl,
            ByteBufCodecs.VAR_LONG.map(ChunkPos::unpack, ChunkPos::pack), RemoveEntitiesMessage::pos,
            ByteBufCodecs.BOOL, RemoveEntitiesMessage::tile,
            ByteBufCodecs.BOOL, RemoveEntitiesMessage::force,
            ByteBufCodecs.BOOL, RemoveEntitiesMessage::All,
            RemoveEntitiesMessage::new
    );

    public static boolean handle(RemoveEntitiesMessage data, IPayloadContext ctx) {
        ServerPlayer player = (ServerPlayer) ctx.player();

        if (!CrashUtils.HasCUPerms(player))
            return true;
        Level world = player.level().getServer().getLevel(data.worldRegistryKey);
        if (data.All) {
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
        return true;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
