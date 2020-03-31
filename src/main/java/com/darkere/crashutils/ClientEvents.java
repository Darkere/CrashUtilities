package com.darkere.crashutils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class ClientEvents {

    @SubscribeEvent
    public void drawEvent(GuiScreenEvent.DrawScreenEvent event) {
        if(!CrashUtils.renderslotnumbers)return;
        if(event.getGui() instanceof ContainerScreen){
            ContainerScreen screen = (ContainerScreen) event.getGui();
            if(screen.getSlotUnderMouse() == null) return;
            screen.renderTooltip("Index: " + screen.getSlotUnderMouse().getSlotIndex(),event.getMouseX(),event.getMouseY());
        }

    }
    @SubscribeEvent
    public void keyevent(GuiScreenEvent.KeyboardKeyPressedEvent.Post event) {
        if(Minecraft.getInstance().player == null || !(event.getGui() instanceof ContainerScreen)) return;
        if(event.getKeyCode() == GLFW.GLFW_KEY_U && event.getModifiers() == GLFW.GLFW_MOD_CONTROL){
            if(Minecraft.getInstance().player.openContainer != null){
                CrashUtils.renderslotnumbers = true;
                String toCopy = Minecraft.getInstance().player.openContainer.getClass().getName();
                Minecraft.getInstance().keyboardListener.setClipboardString(toCopy);
            }
        }
        if(event.getKeyCode() == GLFW.GLFW_KEY_P && event.getModifiers() == GLFW.GLFW_MOD_CONTROL){
            CrashUtils.renderslotnumbers = false;
        }
    }
}
