package com.hack.modules.utility;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * AntiHunger — keeps food and saturation at maximum every tick.
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

        // In 1.21.1 (26.1), LocalPlayer has getFoodData() which returns HungerManager
        p.getFoodData().setFoodLevel(20);
        p.getFoodData().setSaturationLevel(20.0f);
        p.getFoodData().addExhaustion(-1000f);
    }
}
