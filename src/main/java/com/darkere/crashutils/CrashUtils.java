package com.darkere.crashutils;

import com.darkere.crashutils.CrashUtilCommands.EntityCommands.EntitiesCommands;
import com.darkere.crashutils.CrashUtilCommands.HelpCommand;
import com.darkere.crashutils.CrashUtilCommands.InventoryCommands.InventoryCommands;
import com.darkere.crashutils.CrashUtilCommands.ItemClearCommand;
import com.darkere.crashutils.CrashUtilCommands.LoadedChunksCommand;
import com.darkere.crashutils.CrashUtilCommands.MemoryCommand;
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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CrashUtils.MODID)
public class CrashUtils {
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "crashutilities";
    public static final ServerConfig SERVER_CONFIG = new ServerConfig();
    public static boolean curiosLoaded = false;
    Timer chunkcleaner;
    public static boolean sparkLoaded = false;
    public static List<Consumer<ServerLevel>> runnables = new CopyOnWriteArrayList<>();
    public static boolean skipNext = false;
    public static boolean isServer = false;

    public CrashUtils() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::common);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::configReload);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.register(new ClientEvents()));
        //MinecraftForge.EVENT_BUS.register(new DeleteBlocks());
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, ()->new IExtensionPoint.DisplayTest(()->"ANY", (remote, isServer)-> true));
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG.getSpec());
        curiosLoaded = ModList.get().isLoaded("curios");
        sparkLoaded = ModList.get().isLoaded("spark");
    }

    public void common(FMLCommonSetupEvent event) {
        Network.register();
    }

    public void configReload(ModConfigEvent.Reloading event) {
        if(isServer){
            ClearItemTask.restart();
            MemoryChecker.restart();
        }


        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ServerLevel world = server.getLevel(Level.OVERWORLD);
            if (world != null) {
                setupFtbChunksUnloading(world);
            }
        }
    }


    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        CommandNode<CommandSourceStack> entitiesCommands = EntitiesCommands.register();
        CommandNode<CommandSourceStack> tileEntitiesCommands = TileEntitiesCommands.register();
        CommandNode<CommandSourceStack> inventoryCommands = InventoryCommands.register();

        LiteralCommandNode<CommandSourceStack> cmd = dispatcher.register(LiteralArgumentBuilder.<CommandSourceStack>literal(MODID)
                .requires(x -> x.hasPermission(CommandUtils.PERMISSION_LEVEL))
                .then(TeleportCommand.register())
                .then(UnstuckCommand.register())
                .then(MemoryCommand.register())
                .then(ItemClearCommand.register())
                //.then(GetLogCommand.register())
                .then(HelpCommand.register())
                //.then(ProfilingCommands.register())
                .then(LoadedChunksCommand.register())
                .then(ActivityCommand.register())
                .then(entitiesCommands)
                .then(Commands.literal("entities")
                        .redirect(entitiesCommands))
                .then(Commands.literal("entity")
                        .redirect(entitiesCommands))
                .then(tileEntitiesCommands)
                .then(Commands.literal("te")
                        .redirect(tileEntitiesCommands))
                .then(Commands.literal("tileentities")
                        .redirect(tileEntitiesCommands))
                .then(Commands.literal("tileentity")
                        .redirect(tileEntitiesCommands))
                .then(Commands.literal("be")
                    .redirect(tileEntitiesCommands))
                .then(Commands.literal("blockentities")
                    .redirect(tileEntitiesCommands))
                .then(inventoryCommands)
                .then(Commands.literal("inventory")
                        .redirect(inventoryCommands))

        );
        dispatcher.register(Commands.literal("cu")
                .requires(x -> x.hasPermission(CommandUtils.PERMISSION_LEVEL))
                .redirect(cmd)
        );

    }

    @SubscribeEvent
    public void ServerStarted(ServerStartedEvent event) {
        isServer = true;
        ClearItemTask.start();
        MemoryChecker.start();
        setupFtbChunksUnloading(event.getServer().getLevel(Level.OVERWORLD));
    }

    private void setupFtbChunksUnloading(ServerLevel world) {
        if (SERVER_CONFIG.shouldChunksExpire()) {
            chunkcleaner = new Timer(true);
            chunkcleaner.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    PlayerActivityHistory history = new PlayerActivityHistory(world);
                    LOGGER.info("Unloading chunks for players that have not been online in: " + SERVER_CONFIG.getExpireTimeInDays() + " Days");
                    LOGGER.info(history.getPlayersInChunkClearTime().size() + " Player(s) affected ");
                    for (String player : history.getPlayersInChunkClearTime()) {
                        LOGGER.info("Unloading " + player + "'s Chunks");
                        world.getServer().getCommands().performPrefixedCommand(world.getServer().createCommandSourceStack(),
                                "ftbchunks unload_all " + player);
                    }
                }
            }, 5, 60 * 60 * 1000);
        }

    }

    public static void runNextTick(Consumer<ServerLevel> run) {
        runnables.add(run);
    }

    public static void runInTwoTicks(Consumer<ServerLevel> run) {
        runnables.add(run);
        skipNext = true;
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent event) {
        if ( event.phase != TickEvent.Phase.END) return;
        if(event.level.isClientSide && !runnables.isEmpty()){
            runnables.clear();
            return;
        }

        if (!runnables.isEmpty()) {
            if (skipNext) {
                skipNext = false;
                return;
            }
            runnables.forEach(c -> c.accept((ServerLevel) event.level));
            runnables.clear();
        }
    }
}
