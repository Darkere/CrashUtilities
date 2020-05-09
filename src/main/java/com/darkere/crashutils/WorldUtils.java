package com.darkere.crashutils;

import com.darkere.crashutils.Network.Network;
import com.darkere.crashutils.Network.TeleportMessage;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ITeleporter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class WorldUtils{


    public static List<ServerWorld> getWorldsFromDimensionArgument(CommandContext<CommandSource> context){
        DimensionType type = null;
        try {
            type = DimensionArgument.getDimensionArgument(context,"dim");
        }catch (IllegalArgumentException e){
            //NO OP
        }
        List<ServerWorld> worlds = new ArrayList<>();
        if(type == null){
            context.getSource().getServer().getWorlds().forEach(worlds::add);
        } else {
            worlds.add(context.getSource().getServer().getWorld(type));
        }
        return worlds;
    }
    public static void teleportPlayer(ServerPlayerEntity player, DimensionType startWorld, DimensionType destWorld, BlockPos newPos){
        if(player.world.isRemote){
            Network.sendToServer(new TeleportMessage(startWorld,destWorld,newPos));
        }
        if(newPos.getY() == 0){
            ServerWorld world = DimensionManager.getWorld(player.server,destWorld,false,true);
            IChunk chunk = world.getChunkAt(newPos);
            int y = chunk.getTopBlockY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,newPos.getX(),newPos.getZ());
            newPos = new BlockPos(newPos.getX(),y,newPos.getZ());
        }
        if(startWorld != destWorld){
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
            player.setPositionAndUpdate(newPos.getX(),newPos.getY() + 1,newPos.getZ());
        }
    }

    public static void applyToPlayer(String playerName, CommandContext<CommandSource> context, Consumer<ServerPlayerEntity> consumer){
        ServerPlayerEntity player = context.getSource().getServer().getPlayerList().getPlayerByUsername(playerName);
        CommandSource source = context.getSource();
        if(player == null){
            GameProfile profile = source.getServer().getPlayerProfileCache().getGameProfileForUsername(playerName);
            ServerWorld overworld = source.getServer().getWorld(DimensionType.OVERWORLD);
            if(profile == null) source.sendErrorMessage(new StringTextComponent("Player not found"));
            FakePlayer fakePlayer = FakePlayerFactory.get(overworld,profile);
            overworld.getSaveHandler().readPlayerData(fakePlayer);
            consumer.accept(fakePlayer);
            overworld.getSaveHandler().writePlayerData(fakePlayer);
        } else {
            consumer.accept(player);
        }


    }
}
