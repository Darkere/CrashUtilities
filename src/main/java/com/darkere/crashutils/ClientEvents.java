package com.darkere.crashutils;

import com.darkere.crashutils.Screens.CUScreen;
import com.darkere.crashutils.Screens.PlayerInvContainer;
import com.darkere.crashutils.Screens.PlayerInvScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.ITextProperties;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class ClientEvents {

    @SubscribeEvent
    public void drawEvent(GuiScreenEvent.DrawScreenEvent event) {
        if (!CrashUtils.renderslotnumbers) return;
        if (event.getGui() instanceof ContainerScreen) {
            ContainerScreen screen = (ContainerScreen) event.getGui();
            if (screen.getSlotUnderMouse() == null) return;
            screen.renderTooltip(event.getMatrixStack(), ITextProperties.func_240652_a_("Index: " + screen.getSlotUnderMouse().getSlotIndex()), event.getMouseX(), event.getMouseY());
        }

    }

    @SubscribeEvent
    public void GUIKeyEvent(GuiScreenEvent.KeyboardKeyPressedEvent.Post event) {
        if (Minecraft.getInstance().player == null || !(event.getGui() instanceof ContainerScreen)) return;
        if (event.getKeyCode() == GLFW.GLFW_KEY_P && event.getModifiers() == GLFW.GLFW_MOD_CONTROL) {
            if (Minecraft.getInstance().player.openContainer != null) {
                CrashUtils.renderslotnumbers = true;
                String toCopy = Minecraft.getInstance().player.openContainer.getClass().getName();
                Minecraft.getInstance().keyboardListener.setClipboardString(toCopy);
            }
        }
        if (event.getKeyCode() == GLFW.GLFW_KEY_O && event.getModifiers() == GLFW.GLFW_MOD_CONTROL) {
            CrashUtils.renderslotnumbers = false;
        }
    }

    @SubscribeEvent
    public void keyEvent(InputEvent.KeyInputEvent event) {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().world == null) return;
        if (event.getAction() != GLFW.GLFW_PRESS) return;
        if (event.getKey() == GLFW.GLFW_KEY_U && event.getModifiers() == GLFW.GLFW_MOD_CONTROL) {
            RegistryKey<World> worldKey = Minecraft.getInstance().player.getEntityWorld().func_234923_W_();
            if (Minecraft.getInstance().player.hasPermissionLevel(4)) {
                Minecraft.getInstance().displayGuiScreen(new CUScreen(worldKey, new BlockPos(Minecraft.getInstance().player.getPositionVec())));
            } else {
                if (!Minecraft.getInstance().isSingleplayer()) {
                    Minecraft.getInstance().ingameGUI.setOverlayMessage(ITextComponent.func_241827_a_("You need to be OP to use the Crash Utils GUI"), false);
                } else {
                    Minecraft.getInstance().ingameGUI.setOverlayMessage(ITextComponent.func_241827_a_("Cheats need to be enabled to use the Crash Utils GUI"), false);
                }

            }

        }
    }

    public static void openContainerAndScreen(int id, String playerName, Map<String, Integer> curios) {
        PlayerEntity player = Minecraft.getInstance().player;
        if (player == null) return;
        PlayerInvContainer c = new PlayerInvContainer(player, null, id, playerName, curios, curios.values().stream().mapToInt(x -> x).sum());
        player.openContainer = c;
        Minecraft.getInstance().displayGuiScreen(new PlayerInvScreen(c, player.inventory, new StringTextComponent("cuinventoryscreen")));
    }
}
