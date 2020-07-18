package com.darkere.crashutils;

import com.darkere.crashutils.Network.Network;
import com.darkere.crashutils.Network.TeleportMessage;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ITeleporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class WorldUtils {

    private static Map<PlayerEntity, PlayerEntity> playerToContainer = new HashMap<>();

    public static void addPlayerContainerRel(PlayerEntity player1, PlayerEntity containerPlayer) {
        playerToContainer.put(player1, containerPlayer);
    }

    public static PlayerEntity getRelatedContainer(PlayerEntity player) {
        return playerToContainer.get(player);
    }

    public static List<ServerWorld> getWorldsFromDimensionArgument(CommandContext<CommandSource> context) {
        ServerWorld world = null;
        try {
            world = DimensionArgument.getDimensionArgument(context, "dim");
        } catch (IllegalArgumentException | CommandSyntaxException e) {
            //NO OP
        }
        List<ServerWorld> worlds = new ArrayList<>();
        if (world == null) {
            context.getSource().getServer().getWorlds().forEach(worlds::add);
        } else {
            worlds.add(world);
        }
        return worlds;
    }

    public static void teleportPlayer(ServerPlayerEntity player, ServerWorld startWorld, ServerWorld destWorld, BlockPos newPos) {
        if (player.world.isRemote) {
            Network.sendToServer(new TeleportMessage(startWorld.func_234923_W_(), destWorld.func_234923_W_(), newPos));
        }
        if (newPos.getY() == 0) {
            IChunk chunk = destWorld.getChunkAt(newPos);
            int y = chunk.getTopBlockY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, newPos.getX(), newPos.getZ());
            newPos = new BlockPos(newPos.getX(), y, newPos.getZ());
        }
        if (startWorld != destWorld) {
            BlockPos finalNewPos = newPos;
            player.changeDimension(destWorld, new ITeleporter() {
                @Override
                public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                    Entity entity1 = repositionEntity.apply(false);
                    entity1.setPositionAndUpdate(finalNewPos.getX(), finalNewPos.getY() + 1, finalNewPos.getZ());
                    return entity1;
                }
            });
        } else {
            player.setPositionAndUpdate(newPos.getX(), newPos.getY() + 1, newPos.getZ());
        }
    }

    public static void applyToPlayer(String playerName, CommandContext<CommandSource> context, Consumer<ServerPlayerEntity> consumer) {
        ServerPlayerEntity player = context.getSource().getServer().getPlayerList().getPlayerByUsername(playerName);
        CommandSource source = context.getSource();
        if (player == null) {
            MinecraftServer server = source.getServer();
            GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(playerName);
            if (profile == null){
                source.sendErrorMessage(new StringTextComponent("Player not found"));
                return;
            }
            FakePlayer fakePlayer = new FakePlayer(server.getWorld(World.field_234918_g_),profile);
            CompoundNBT nbt = server.field_240766_e_.func_237336_b_(fakePlayer);
            if(nbt == null) return;
            fakePlayer.read(nbt);
            consumer.accept(fakePlayer);
            server.field_240766_e_.func_237335_a_(fakePlayer);


        } else {
            consumer.accept(player);
        }


    }
}
