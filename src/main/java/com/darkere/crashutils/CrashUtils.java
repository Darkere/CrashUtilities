package com.darkere.crashutils;

import com.darkere.crashutils.CrashUtilCommands.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CrashUtils.MODID)
public class CrashUtils {
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    static boolean renderslotnumbers;
    public static final String MODID = "crashutilities";
    public static final ServerConfig SERVER_CONFIG = new ServerConfig();
    ClearItemTask task;
    public static MemoryChecker memoryChecker = null;
    public static boolean curiosLoaded = false;
    public CrashUtils() {

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(new ClientEvents()));
        MinecraftForge.EVENT_BUS.register(new DeleteBlocks());
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG.getSpec());
    }

    @SubscribeEvent
    public void configReload(ModConfig.Reloading event){
        task.setup();
        memoryChecker.setup();
    }



    @SubscribeEvent
    public void serverStarting(FMLServerStartingEvent event) {
        curiosLoaded = ModList.get().isLoaded("curios");
        CommandDispatcher<CommandSource> dispatcher = event.getCommandDispatcher();
        LiteralCommandNode<CommandSource> cmd = dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal(MODID)
            .requires(x-> x.hasPermissionLevel(4))
            .then(AllLoadedTEsCommand.register())
            .then(FindLoadedTEsCommand.register())
            .then(CrashCommand.register())
            .then(AllEntitiesCommand.register())
            .then(FindEntitiesCommand.register())
            .then(TeleportCommand.register())
            .then(UnstuckCommand.register())
            .then(MemoryCommand.register())
            .then(ItemClearCommand.register())
            .then(InventoryRemovalCommand.register())
            .then(InventoryLookCommand.register())
            .then(RemoveFromInventorySlotCommand.register())

        );
        dispatcher.register(Commands.literal("cu").redirect(cmd));
    }
    @SubscribeEvent
    public void ServerStarted(FMLServerStartedEvent event){
        Timer timer = new Timer();
        task = new ClearItemTask();
        task.setup();
        int time = SERVER_CONFIG.getTimer()* 60 * 1000;
        timer.scheduleAtFixedRate(task,5,time);
        if(SERVER_CONFIG.getMemoryChecker()){
            memoryChecker = new MemoryChecker();
            memoryChecker.setup();
            time = SERVER_CONFIG.getMemoryTimer() * 1000;
            timer.scheduleAtFixedRate(memoryChecker,5,time);
        }

    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.isRemote) return;
        task.checkItemCounts((ServerWorld)event.world);

    }
}
