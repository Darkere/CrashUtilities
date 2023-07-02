package com.darkere.crashutils;

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
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class ClientEvents {

    public static final KeyMapping OPENSCREEN =
        new KeyMapping("crashutils.openscreen",
            KeyConflictContext.IN_GAME, KeyModifier.CONTROL,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_U, "crashutils.keycategory");
    public static final KeyMapping COPYCLASS =
        new KeyMapping("crashutils.copycontainer",
            KeyConflictContext.GUI, InputConstants.UNKNOWN, "crashutils.keycategory");
    public static final KeyMapping TOGGLEINDEXES =
        new KeyMapping("crashutils.slottooltips",
            KeyConflictContext.IN_GAME, InputConstants.UNKNOWN, "crashutils.keycategory");

    private static boolean renderslotnumbers;

    @SubscribeEvent
    public static void registerKeybindings(RegisterKeyMappingsEvent event) {
        event.register(OPENSCREEN);
        event.register(COPYCLASS);
        event.register(TOGGLEINDEXES);
    }

    @SubscribeEvent
    public void drawEvent(ScreenEvent.Render event) {
        if (!renderslotnumbers) return;
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen) {
            if (screen.getSlotUnderMouse() == null) return;
            event.getGuiGraphics().renderTooltip(Minecraft.getInstance().font,CommandUtils.CreateTextComponent("Index: " + screen.getSlotUnderMouse().getSlotIndex()), event.getMouseX(), event.getMouseY());
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
            ResourceKey<Level> worldKey = Minecraft.getInstance().player.getCommandSenderWorld().dimension();
            if (Minecraft.getInstance().player.hasPermissions(CommandUtils.PERMISSION_LEVEL)) {
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
        Minecraft.getInstance().setScreen(new PlayerInvScreen(c, player.getInventory(), CommandUtils.CreateTextComponent("cuinventoryscreen")));
    }
}
