package com.darkere.crashutils;

import com.darkere.crashutils.Screens.PlayerInvContainer;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;


public class CURegistry {
    static ContainerType<PlayerInvContainer> playerInvContainerContainerType;


    public static final DeferredRegister<ContainerType<?>> CONTAINER = new DeferredRegister<>(ForgeRegistries.CONTAINERS,CrashUtils.MODID);

    public static void init(){
        CONTAINER.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<ContainerType<PlayerInvContainer>> PLAYER_INV_CONTAINER = CONTAINER.register("cuinventory",()-> IForgeContainerType.create((id, inv, data)-> new PlayerInvContainer(playerInvContainerContainerType, inv.player, data, id)));
}

