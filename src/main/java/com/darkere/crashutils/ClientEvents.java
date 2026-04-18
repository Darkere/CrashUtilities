package com.darkere.crashutils;

import com.darkere.crashutils.Network.*;
import com.darkere.crashutils.Screens.CUScreen;
import com.darkere.crashutils.Screens.PlayerInvContainer;
import com.darkere.crashutils.Screens.PlayerInvScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class ClientEvents {

    public static KeyMapping.Category CrashUtilsCat = new KeyMapping.Category(CrashUtils.Identifier("keys"));
    public static KeyMapping OPENSCREEN;

    public static KeyMapping COPYCLASS;
    public static KeyMapping TOGGLEINDEXES;

    private static boolean renderslotnumbers;

    public static void registerKeybindings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CrashUtilsCat);
        OPENSCREEN = new KeyMapping("crashutils.openscreen",
                KeyConflictContext.IN_GAME, KeyModifier.CONTROL,
                InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U,CrashUtilsCat);
        event.register(OPENSCREEN);
        COPYCLASS =
                new KeyMapping("crashutils.copycontainer",
                        KeyConflictContext.GUI, InputConstants.UNKNOWN, CrashUtilsCat);
        event.register(COPYCLASS);
        TOGGLEINDEXES =
                new KeyMapping("crashutils.slottooltips",
                        KeyConflictContext.IN_GAME, InputConstants.UNKNOWN, CrashUtilsCat);
        event.register(TOGGLEINDEXES);
    }

    @SubscribeEvent
    public void drawEvent(ScreenEvent.Render.Pre event) {
        if (!renderslotnumbers) return;
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
            if (screen.getHoveredSlot() == null) return;
            event.getGuiGraphics().setTooltipForNextFrame(Minecraft.getInstance().font,CommandUtils.CreateTextComponent("Index: " + screen.getHoveredSlot().getSlotIndex()), event.getMouseX(), event.getMouseY());
        }

    }

    @SubscribeEvent
    public void GUIKeyEvent(ScreenEvent.KeyPressed.Post event) {
        if (Minecraft.getInstance().player == null || !(event.getScreen() instanceof AbstractContainerScreen)) return;
        if (COPYCLASS.consumeClick()) {
            String toCopy = Minecraft.getInstance().player.containerMenu.getClass().getName();
            Minecraft.getInstance().keyboardHandler.setClipboard(toCopy);
        }
        if (TOGGLEINDEXES.consumeClick()) {
            renderslotnumbers = !renderslotnumbers;
        }
    }

    @SubscribeEvent
    public void keyEvent(InputEvent.Key event) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) return;
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        if (OPENSCREEN.consumeClick()) {
            ResourceKey<Level> worldKey = Minecraft.getInstance().player.level().dimension();
            if (CrashUtils.HasCUPerms(Minecraft.getInstance().player)) {
                var pos = Minecraft.getInstance().player.position();
                var posi = new Vec3i ((int)pos.x,(int)pos.y,(int)pos.z);
                Minecraft.getInstance().setScreen(CUScreen.openCUScreen(worldKey, new BlockPos(posi)));
            } else {
                if (!Minecraft.getInstance().hasSingleplayerServer()) {
                    Minecraft.getInstance().gui.setOverlayMessage(CommandUtils.CreateTextComponent("You need to be OP to use the Crash Utils GUI"), false);
                } else {
                    Minecraft.getInstance().gui.setOverlayMessage(CommandUtils.CreateTextComponent("Cheats need to be enabled to use the Crash Utils GUI"), false);
                }

            }

        }
    }

    public static void openContainerAndScreen(int id, String playerName, Map<String, Integer> curios) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        PlayerInvContainer c = new PlayerInvContainer(player, /*clientside null */null, id, playerName, curios, curios.values().stream().mapToInt(x -> x).sum());
        player.containerMenu = c;
        Minecraft.getInstance().setScreen(new PlayerInvScreen(c, player.getInventory(), CommandUtils.CreateTextComponent(playerName + "'s Inventory")));
    }

    public static void registerClientNetworking(RegisterClientPayloadHandlersEvent event) {
         event.register(UpdateDataRequestMessage.TYPE, UpdateDataRequestMessage::handle);
         event.register(EntityDataMessage.TYPE, EntityDataMessage::handle);
         event.register(TeleportMessage.TYPE,  TeleportMessage::handle);
         event.register(PlayerInventoryRequestMessage.TYPE,  PlayerInventoryRequestMessage::handle);
         event.register(PlayerDataMessage.TYPE,  PlayerDataMessage::handle);
         event.register(OpenPlayerInvMessage.TYPE,  OpenPlayerInvMessage::handle);
         event.register(TeleportToPlayerMessage.TYPE,  TeleportToPlayerMessage::handle);
         event.register(RemoveEntitiesMessage.TYPE, RemoveEntitiesMessage::handle);
         event.register(RemoveEntityMessage.TYPE, RemoveEntityMessage::handle);
         event.register(OpenEnderChestMessage.TYPE, OpenEnderChestMessage::handle);
         event.register(LoadedChunkDataStateMessage.TYPE, LoadedChunkDataStateMessage::handle);
         event.register(TileEntityDataMessage.TYPE, TileEntityDataMessage::handle);
         event.register(LoadedChunkDataTicketsMessage.TYPE, LoadedChunkDataTicketsMessage::handle);
    }
}
