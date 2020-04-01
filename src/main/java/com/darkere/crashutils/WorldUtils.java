package com.darkere.crashutils;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
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
        if(startWorld != destWorld){
            player.changeDimension(destWorld, new ITeleporter() {
                @Override
                public Entity placeEntity(Entity entity, ServerWorld currentWorld, ServerWorld destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
                    Entity entity1 = repositionEntity.apply(false);
                    entity1.setPositionAndUpdate(newPos.getX(),newPos.getY() + 1,newPos.getZ());
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
