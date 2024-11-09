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
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
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
    public static List<Runnable> runnablesClient = new CopyOnWriteArrayList<>();
    public static boolean skipNext = false;
    public static boolean skipNextClient = false;
    public static boolean isServer = false;

    public static ResourceLocation ResourceLocation(String Path){
        return ResourceLocation.fromNamespaceAndPath(MODID,Path);
    }

    public CrashUtils(IEventBus ModEventBus, ModContainer modContainer) {
        ModEventBus.addListener(this::configReload);
        ModEventBus.addListener(Network::register);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(new ClientEvents());
            ModEventBus.addListener(ClientEvents::registerKeybindings);
        }
        NeoForge.EVENT_BUS.register(this);
        modContainer.registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG.getSpec());
        curiosLoaded = ModList.get().isLoaded("curios");
        sparkLoaded = ModList.get().isLoaded("spark");
    }

    public void configReload(ModConfigEvent.Reloading event) {
        if (isServer) {
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
    public static void runNextTickClient(Runnable run) {
        runnablesClient.add(run);
    }

    public static void runInTwoTicksClient(Runnable run) {
        runnablesClient.add(run);
        skipNextClient = true;
    }

    @SubscribeEvent
    public void onWorldTick(ServerTickEvent.Post event) {
        if (!runnables.isEmpty()) {
            if (skipNext) {
                skipNext = false;
                return;
            }
            var level = event.getServer().getLevel(Level.OVERWORLD);
            runnables.forEach(c -> c.accept(level));
            runnables.clear();
        }
    }
    @SubscribeEvent
    public void onWorldTick(ClientTickEvent.Post event) {
        if (!runnables.isEmpty()) {
            if (skipNext) {
                skipNext = false;
                return;
            }
            runnablesClient.forEach(Runnable::run);
            runnablesClient.clear();
        }
    }
}
