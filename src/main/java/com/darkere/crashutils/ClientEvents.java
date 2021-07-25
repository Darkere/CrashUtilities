package com.darkere.crashutils;

import com.darkere.crashutils.Screens.CUScreen;
import com.darkere.crashutils.Screens.PlayerInvContainer;
import com.darkere.crashutils.Screens.PlayerInvScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class ClientEvents {

    public static final KeyBinding OPENSCREEN =
        new KeyBinding("Open the Crash Utilities Screen",
            KeyConflictContext.IN_GAME, KeyModifier.CONTROL,
            InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_U, "Crash Utilities");
    public static final KeyBinding COPYCLASS =
        new KeyBinding("Copy Container Class",
            KeyConflictContext.GUI, InputMappings.UNKNOWN, "Crash Utilities");
    public static final KeyBinding TOGGLEINDEXES =
        new KeyBinding("Show Slot Index Tooltips",
            KeyConflictContext.IN_GAME, InputMappings.UNKNOWN, "Crash Utilities");

    private static boolean renderslotnumbers;

    public static void registerKeybindings() {
        ClientRegistry.registerKeyBinding(OPENSCREEN);
        ClientRegistry.registerKeyBinding(COPYCLASS);
        ClientRegistry.registerKeyBinding(TOGGLEINDEXES);
    }

    @SubscribeEvent
    public void drawEvent(GuiScreenEvent.DrawScreenEvent event) {
        if (!renderslotnumbers) return;
        if (event.getGui() instanceof ContainerScreen) {
            ContainerScreen<?> screen = (ContainerScreen<?>) event.getGui();
            if (screen.getSlotUnderMouse() == null) return;
            screen.renderTooltip(event.getMatrixStack(), new StringTextComponent("Index: " + screen.getSlotUnderMouse().getSlotIndex()), event.getMouseX(), event.getMouseY());
        }

    }

    @SubscribeEvent
    public void GUIKeyEvent(GuiScreenEvent.KeyboardKeyPressedEvent.Post event) {
        if (Minecraft.getInstance().player == null || !(event.getGui() instanceof ContainerScreen)) return;
        if (COPYCLASS.consumeClick()) {
            if (Minecraft.getInstance().player.containerMenu != null) {
                String toCopy = Minecraft.getInstance().player.containerMenu.getClass().getName();
                Minecraft.getInstance().keyboardHandler.setClipboard(toCopy);
            }
        }
        if (TOGGLEINDEXES.consumeClick()) {
            renderslotnumbers = !renderslotnumbers;
        }
    }

    @SubscribeEvent
    public void keyEvent(InputEvent.KeyInputEvent event) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) return;
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        if (OPENSCREEN.consumeClick()) {
            RegistryKey<World> worldKey = Minecraft.getInstance().player.getCommandSenderWorld().dimension();
            if (Minecraft.getInstance().player.hasPermissions(CommandUtils.PERMISSION_LEVEL)) {
                Minecraft.getInstance().setScreen(CUScreen.openCUScreen(worldKey, new BlockPos(Minecraft.getInstance().player.position())));
            } else {
                if (!Minecraft.getInstance().hasSingleplayerServer()) {
                    Minecraft.getInstance().gui.setOverlayMessage(new StringTextComponent("You need to be OP to use the Crash Utils GUI"), false);
                } else {
                    Minecraft.getInstance().gui.setOverlayMessage(new StringTextComponent("Cheats need to be enabled to use the Crash Utils GUI"), false);
                }

            }

        }
    }

    public static void openContainerAndScreen(int id, String playerName, Map<String, Integer> curios) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;
        PlayerInvContainer c = new PlayerInvContainer(player, /*clientside null */null, id, playerName, curios, curios.values().stream().mapToInt(x -> x).sum());
        player.containerMenu = c;
        Minecraft.getInstance().setScreen(new PlayerInvScreen(c, player.inventory, new StringTextComponent("cuinventoryscreen")));
    }
}
