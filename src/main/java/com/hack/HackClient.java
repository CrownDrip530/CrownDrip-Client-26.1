package com.hack;

import net.fabricmc.api.ClientModInitializer;

/**
 * HackClient - Entry point. Called once when Minecraft starts.
 *
 * Creates ModuleManager and registers all hacks.
 * moduleManager is public-static so any mixin can safely check:
 *   if (HackClient.moduleManager == null) return;
 * before accessing it during early game load.
 */
public class HackClient implements ClientModInitializer {

    public static ModuleManager moduleManager = null;

    @Override
    public void onInitializeClient() {
        moduleManager = new ModuleManager();
        moduleManager.init();
        System.out.println("[HackClient] Ready. Press G for GUI. Type .binds in chat.");
    }
}
