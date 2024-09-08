package com.darkere.crashutils.Network;

import com.darkere.crashutils.CommandUtils;
import com.darkere.crashutils.CrashUtils;
import com.darkere.crashutils.WorldUtils;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record RemoveEntityMessage(ResourceKey<Level> worldRegistryKey, UUID uuid, boolean tile,boolean force ) implements CustomPacketPayload {
    public static final Type<RemoveEntityMessage> TYPE = new Type<>(CrashUtils.ResourceLocation("removeentitymessage"));
    public static final StreamCodec<RegistryFriendlyByteBuf,RemoveEntityMessage> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), RemoveEntityMessage::worldRegistryKey,
            UUIDUtil.STREAM_CODEC,RemoveEntityMessage::uuid,
            ByteBufCodecs.BOOL,RemoveEntityMessage::tile,
            ByteBufCodecs.BOOL,RemoveEntityMessage::force,
            RemoveEntityMessage::new
    );


    public static boolean handle(RemoveEntityMessage data, IPayloadContext ctx) {
            ServerPlayer player = (ServerPlayer) ctx.player();
            if (!player.hasPermissions(CommandUtils.PERMISSION_LEVEL)) return true;
            if (data.tile) {
                WorldUtils.removeTileEntity(player.getServer().getLevel(data.worldRegistryKey), data.uuid, data.force);
            } else {
                WorldUtils.removeEntity(player.getServer().getLevel(data.worldRegistryKey), data.uuid);
            }
        return true;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
