package com.hack.modules.utility;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * AntiHunger — keeps food and saturation at maximum every tick.
 *
 * HOW IT WORKS:
 * Sets foodLevel to 20 and saturationLevel to 20.0 every tick.
 * Client-side only — server still tracks hunger independently,
 * but prevents the client from sending exhaustion packets
 * that would reduce server-side hunger.
 * Also prevents the slowness effect from low hunger client-side.
 */
public class AntiHunger extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    public AntiHunger() {
        super("AntiHunger", "Utility");
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;

        p.getHungerManager().setFoodLevel(20);
        p.getHungerManager().setSaturationLevel(20.0f);
        p.getHungerManager().addExhaustion(-1000f); // cancel exhaustion buildup
    }
}
