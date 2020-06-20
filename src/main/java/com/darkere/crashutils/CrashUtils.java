package com.darkere.crashutils;

import com.darkere.crashutils.CrashUtilCommands.*;
import com.darkere.crashutils.Network.Network;
import com.darkere.crashutils.Screens.PlayerInvScreen;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.imc.CurioIMCMessage;

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
    Timer timer;
    public static boolean runHeapDump = false;
    public static boolean sparkLoaded = false;
    public CrashUtils() {
        new CURegistry();
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::common);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::client);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::intermod);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(new ClientEvents()));
        MinecraftForge.EVENT_BUS.register(new DeleteBlocks());
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG.getSpec());


    }
    public void intermod(InterModEnqueueEvent e){
        InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE,()-> new CurioIMCMessage("necklace").setEnabled(true).setSize(1).setHidden(false));
        InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE,()-> new CurioIMCMessage("head").setEnabled(true).setSize(3).setHidden(false));
        InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE,()-> new CurioIMCMessage("2").setEnabled(true).setSize(5).setHidden(false));
        InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE,()-> new CurioIMCMessage("he3ad").setEnabled(true).setSize(3).setHidden(false));
        InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE,()-> new CurioIMCMessage("he4ad").setEnabled(true).setSize(1).setHidden(false));
        InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE,()-> new CurioIMCMessage("hea5d").setEnabled(true).setSize(1).setHidden(false));
        InterModComms.sendTo("curios", CuriosAPI.IMC.REGISTER_TYPE,()-> new CurioIMCMessage("12787").setEnabled(true).setSize(2).setHidden(false));
    }
    public void client(FMLClientSetupEvent event){
        ScreenManager.registerFactory(CURegistry.PLAYER_INV_CONTAINER.get(), PlayerInvScreen::new);
    }

    public void common(FMLCommonSetupEvent event){
        Network.register();
    }
    @SubscribeEvent
    public void configReload(ModConfig.Reloading event) {
        task.setup();
        memoryChecker.setup();
    }


    @SubscribeEvent
    public void serverStarting(FMLServerStartingEvent event) {
        curiosLoaded = ModList.get().isLoaded("curios");
        sparkLoaded = ModList.get().isLoaded("spark");
        CommandDispatcher<CommandSource> dispatcher = event.getCommandDispatcher();
        LiteralCommandNode<CommandSource> cmd = dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal(MODID)
            .requires(x -> x.hasPermissionLevel(4))
            .then(AllLoadedTEsCommand.register())
            .then(FindLoadedTEsCommand.register())
            .then(AllEntitiesCommand.register())
            .then(FindEntitiesCommand.register())
            .then(TeleportCommand.register())
            .then(UnstuckCommand.register())
            .then(MemoryCommand.register())
            .then(ItemClearCommand.register())
            .then(InventoryRemovalCommand.register())
            .then(InventoryLookCommand.register())
            .then(RemoveFromInventorySlotCommand.register())
            .then(GetLogCommand.register())
            //.then(ProfilingCommands.register())
            .then(LoadedChunksCommand.register())

        );
        dispatcher.register(Commands.literal("cu").redirect(cmd));

    }
    @SubscribeEvent
    public void ServerStopping(FMLServerStoppingEvent event){
        timer.cancel();
    }

    @SubscribeEvent
    public void ServerStarted(FMLServerStartedEvent event) {
        timer = new Timer();
        task = new ClearItemTask();
        task.setup();
        int time = SERVER_CONFIG.getTimer() * 60 * 1000;
        timer.scheduleAtFixedRate(task, time, time);
        if (SERVER_CONFIG.getMemoryChecker()) {
            memoryChecker = new MemoryChecker();
            memoryChecker.setup();
            time = SERVER_CONFIG.getMemoryTimer() * 1000;
            timer.scheduleAtFixedRate(memoryChecker, time, time);
        }

    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.isRemote) return;
        task.checkItemCounts((ServerWorld) event.world);
        if(sparkLoaded && runHeapDump){
            runHeapDump = false;
            event.world.getServer().getCommandManager().handleCommand(event.world.getServer().getCommandSource(),"/spark heapdump");
        }

    }

}
