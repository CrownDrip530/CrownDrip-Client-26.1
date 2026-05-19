package com.hack.modules.combat;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Criticals — makes every attack a critical hit by jumping just before striking.
 */
public class Criticals extends HackModule {
    private final Minecraft mc = Minecraft.getInstance();

    public Criticals() {
        super("Criticals", "Combat");
    }

    public void prepareCrit() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;
        if (p.getAbilities().flying) return;
        if (!p.onGround()) return;

        p.setDeltaMovement(p.getDeltaMovement().x, 0.11, p.getDeltaMovement().z);
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;
        if (p.getAbilities().flying) return;
    }
}
