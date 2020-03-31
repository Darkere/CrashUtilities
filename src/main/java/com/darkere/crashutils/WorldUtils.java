package com.darkere.crashutils;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import jdk.nashorn.internal.ir.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ITeleporter;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.ArrayList;
import java.util.List;
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
    public static void teleportOfflinePlayer(String playerName, CommandSource source){
        MinecraftServer server = source.getServer();
        ServerWorld overworld = server.getWorld(DimensionType.OVERWORLD);
        BlockPos spawn = overworld.getSpawnPoint();
        GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(playerName);
        if(profile == null) source.sendErrorMessage(new StringTextComponent("Player not found"));
        FakePlayer player = FakePlayerFactory.get(overworld,profile);
        player.setPosition(spawn.getX(),spawn.getY(),spawn.getZ());
        player.dimension = DimensionType.OVERWORLD;
        overworld.getSaveHandler().writePlayerData(player);
        source.sendFeedback(new StringTextComponent("Sent Player "+ playerName+ " to Spawn"),true);
    }
}
