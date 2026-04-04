package com.hack.mixins;

import com.hack.HackClient;
import com.hack.gui.ClickGUI;
import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * KeyInputMixin — game tick hook.
 * 1. Calls onTick() on every enabled module (critical — drives FlyHack etc.)
 * 2. G key opens/closes ClickGUI
 * 3. Checks module keybinds assigned via .bind or GUI right-click
 */
@Mixin(Minecraft.class)
public class KeyInputMixin {

    private boolean   prevGKey       = false;
    private boolean[] prevModuleKeys = new boolean[0];

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (HackClient.moduleManager == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        long window = mc.getWindow().getHandle();

        // 1 — dispatch onTick() to all enabled modules
        for (HackModule module : HackClient.moduleManager.getAllModules()) {
            if (module.isEnabled()) module.onTick();
        }

        // 2 — G key: open ClickGUI only when no screen is open (rising edge)
        // Closing is handled by ClickGUI.keyPressed() to avoid double-firing
        boolean gNow = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_G) == GLFW.GLFW_PRESS;
        if (gNow && !prevGKey) {
            if (mc.currentScreen == null) {
                mc.setScreen(new ClickGUI());
            }
            // NOTE: closing is handled exclusively by ClickGUI.keyPressed(G) -> close()
        }
        prevGKey = gNow;

        // 3 — module keybinds (rising edge, skip G)
        List<HackModule> modules = HackClient.moduleManager.getAllModules();
        if (prevModuleKeys.length != modules.size()) {
            prevModuleKeys = new boolean[modules.size()];
        }
        for (int i = 0; i < modules.size(); i++) {
            HackModule m = modules.get(i);
            int key = m.getKeybind();
            if (key == -1 || key == GLFW.GLFW_KEY_G) {
                prevModuleKeys[i] = false;
                continue;
            }
            boolean keyNow = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
            if (keyNow && !prevModuleKeys[i]) m.toggle();
            prevModuleKeys[i] = keyNow;
        }
    }
}
