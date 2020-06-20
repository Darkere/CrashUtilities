package com.darkere.crashutils;

import com.darkere.crashutils.Screens.PlayerInvContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class CURegistry {
    static ContainerType<PlayerInvContainer> playerInvContainerContainerType;
    private static Map<PlayerEntity,PlayerEntity> playerToContainer = new HashMap<>();

    public static void addPlayerContainerRel(PlayerEntity player1, PlayerEntity containerPlayer){
        playerToContainer.put(player1,containerPlayer);
    }
    public static PlayerEntity getRelatedContainer(PlayerEntity player){
        return playerToContainer.get(player);
    }

    public static final DeferredRegister<ContainerType<?>> CONTAINER = new DeferredRegister<>(ForgeRegistries.CONTAINERS,CrashUtils.MODID);

    CURegistry(){
        CONTAINER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<ContainerType<PlayerInvContainer>> PLAYER_INV_CONTAINER = CONTAINER.register("cuinventory",()-> IForgeContainerType.create((id, inv, data)-> new PlayerInvContainer(playerInvContainerContainerType, inv.player, data, id)));
}

