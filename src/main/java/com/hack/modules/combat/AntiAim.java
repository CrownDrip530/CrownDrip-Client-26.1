package com.hack.modules.combat;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;

/**
 * AntiAim — spins your head randomly to confuse enemy KillAura targeting.
 * Mode 0 = Spin, Mode 1 = Backwards, Mode 2 = Random
 */
public class AntiAim extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    public final Setting modeSetting = new Setting("Mode", 0.0f, 0.0f, 2.0f);
    private int tick = 0;

    public AntiAim() {
        super("AntiAim", "Combat");
        settings.add(modeSetting);
        alwaysShowSettings = true;
    }

    @Override
    public void onTick() {
        if (!isEnabled() || mc.player == null) return;
        tick++;
        int mode = (int) modeSetting.value;
        if (mode == 0) { // Spin
            mc.player.setYaw(mc.player.getYaw() + 30);
        } else if (mode == 1) { // Backwards
            mc.player.setYaw(mc.player.getYaw() + 180);
        } else { // Random
            mc.player.setYaw((float)(Math.random() * 360));
            mc.player.setPitch((float)(Math.random() * 180 - 90));
        }
    }
}
