package com.darkere.crashutils;

import com.darkere.crashutils.CrashUtilCommands.EntityCommands.EntitiesCommands;
import com.darkere.crashutils.CrashUtilCommands.*;
import com.darkere.crashutils.CrashUtilCommands.InventoryCommands.InventoryCommands;
import com.darkere.crashutils.CrashUtilCommands.PlayerCommands.ActivityCommand;
import com.darkere.crashutils.CrashUtilCommands.PlayerCommands.TeleportCommand;
import com.darkere.crashutils.CrashUtilCommands.PlayerCommands.UnstuckCommand;
import com.darkere.crashutils.CrashUtilCommands.TileEntityCommands.TileEntitiesCommands;
import com.darkere.crashutils.DataStructures.PlayerActivityHistory;
import com.darkere.crashutils.Network.Network;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CrashUtils.MODID)
public class CrashUtils {
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    static boolean renderslotnumbers;
    public static final String MODID = "crashutilities";
    public static final ServerConfig SERVER_CONFIG = new ServerConfig();
    ClearItemTask clearItemTask;
    public static MemoryChecker memoryChecker = null;
    public static boolean curiosLoaded = false;
    Timer chunkcleaner;
    public static boolean runHeapDump = false;
    public static boolean sparkLoaded = false;
    public static List<Runnable> runnables = new ArrayList<>();
    public static boolean intwoTicks = false;

    public CrashUtils() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::common);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::client);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(new ClientEvents()));
        //MinecraftForge.EVENT_BUS.register(new DeleteBlocks());
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG.getSpec());
        curiosLoaded = ModList.get().isLoaded("curios");
        sparkLoaded = ModList.get().isLoaded("spark");
    }

    public void client(FMLClientSetupEvent event) {
        ClientEvents.registerKeybindings();
    }


    public void common(FMLCommonSetupEvent event) {
        Network.register();
    }

    @SubscribeEvent
    public void configReload(ModConfig.Reloading event) {
        clearItemTask.startStopTask();
        memoryChecker.startStopMemoryChecker();

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if(server != null){
            ServerWorld world = server.getWorld(World.OVERWORLD);
            if(world != null) {
                setupFtbChunksUnloading(world);
            }
        }
    }


    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
        CommandNode<CommandSource> entitiesCommands = EntitiesCommands.register();
        CommandNode<CommandSource> tileEntitiesCommands = TileEntitiesCommands.register();
        CommandNode<CommandSource> inventoryCommands = InventoryCommands.register();

        LiteralCommandNode<CommandSource> cmd = dispatcher.register(LiteralArgumentBuilder.<CommandSource>literal(MODID)
            .requires(x -> x.hasPermissionLevel(2))
            .then(TeleportCommand.register())
            .then(UnstuckCommand.register())
            .then(MemoryCommand.register())
            .then(ItemClearCommand.register())
            .then(GetLogCommand.register())
            .then(HelpCommand.register())
            //.then(ProfilingCommands.register())
            .then(LoadedChunksCommand.register())
            .then(ActivityCommand.register())
            .then(entitiesCommands)
            .then(Commands.literal("e")
                .redirect(entitiesCommands))
            .then(tileEntitiesCommands)
            .then(Commands.literal("te")
                .redirect(tileEntitiesCommands))
            .then(inventoryCommands)
            .then(Commands.literal("i")
                .redirect(inventoryCommands))

        );
        dispatcher.register(Commands.literal("cu")
                .requires(x -> x.hasPermissionLevel(2))
                .redirect(cmd)
        );

    }

    @SubscribeEvent
    public void ServerStarted(FMLServerStartedEvent event) {
        clearItemTask = new ClearItemTask();
        memoryChecker = new MemoryChecker();

        setupFtbChunksUnloading(event.getServer().getWorld(World.OVERWORLD));
    }

    private void setupFtbChunksUnloading(ServerWorld world) {
        if (SERVER_CONFIG.shouldChunksExpire()) {
            chunkcleaner = new Timer(true);
            chunkcleaner.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    PlayerActivityHistory history = new PlayerActivityHistory(world);
                    LOGGER.info("Unloading chunks for players that have not been online in: " + SERVER_CONFIG.getExpireTimeInDays() + " Days");
                    LOGGER.info(history.getPlayersInChunkClearTime() + " Player(s) affected ");
                    for (String player : history.getPlayersInChunkClearTime()) {
                        LOGGER.info("Unloading " + player + "'s Chunks");
                        world.getServer().getCommandManager().handleCommand(world.getServer().getCommandSource(),
                            "ftbchunks unload_all " + player);
                    }
                }
            }, 5, 60*60*1000);
        }

    }

    public static void runNextTick(Runnable run) {
        runnables.add(run);
    }

    public static void runInTwoTicks(Runnable run) {
        runnables.add(run);
        intwoTicks = true;
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event) {
        if (event.world.isRemote && event.phase != TickEvent.Phase.END) return;
        clearItemTask.checkItemCounts((ServerWorld) event.world);

        if (sparkLoaded && runHeapDump) {
            runHeapDump = false;
            event.world.getServer().sendMessage(new StringTextComponent("Running Heapdump. Massive Lagspike incoming!"), null);
            event.world.getServer().getCommandManager().handleCommand(event.world.getServer().getCommandSource(), "/spark heapdump");
        }
        if (intwoTicks) {
            intwoTicks = false;
            return;
        }
        if (!runnables.isEmpty()) {
            runnables.forEach(Runnable::run);
            runnables.clear();
        }

    }

}
