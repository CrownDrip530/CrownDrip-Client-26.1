package com.hack.modules.combat;

import com.hack.HackClient;
import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Criticals — makes every attack a critical hit by jumping just before striking.
 *
 * Simplified to one mode: Jump mode.
 * Packet mode was unreliable in 1.21.11, jump mode works consistently.
 *
 * HOW IT WORKS:
 * A critical hit requires the player to be falling (negative Y velocity)
 * when the attack lands. We give a tiny hop (setVelocity Y = 0.11)
 * just before every attack so the player is always in a micro-fall.
 */
public class Criticals extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    public Criticals() {
        super("Criticals", "Combat");
    }

    /**
     * Called by KillAura and AutoClicker before attacking.
     */
    public void prepareCrit() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;
        if (p.getAbilities().flying) return;
        if (!p.isOnGround()) return;

        // Tiny hop that puts player in a falling state next tick
        p.setVelocity(p.getVelocity().x, 0.11, p.getVelocity().z);
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;
        if (p.getAbilities().flying) return;

        // If KillAura is enabled, it handles calling prepareCrit()
        // This onTick handles standalone use with AutoClicker
    }
}
