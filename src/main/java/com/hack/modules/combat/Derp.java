package com.hack.modules.combat;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;

/**
 * Derp — looks straight up or down alternating every tick.
 * Looks hilarious. Confuses some combat anticheats.
 */
public class Derp extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    private int tick = 0;

    public Derp() { super("Derp", "Combat"); }

    @Override
    public void onTick() {
        if (!isEnabled() || mc.player == null) return;
        mc.player.setXRot(tick % 2 == 0 ? 90.0f : -90.0f);
        tick++;
    }
}
