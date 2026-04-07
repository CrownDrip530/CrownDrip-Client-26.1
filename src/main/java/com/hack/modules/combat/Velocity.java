package com.hack.modules.combat;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Velocity — reduces or cancels knockback when you get hit.
 *
 * HOW IT WORKS:
 * When you get hit the server sends a velocity packet that pushes you back.
 * We intercept large sudden velocity spikes (the signature of knockback)
 * and reduce them by the configured percentage.
 *
 * Horizontal slider: how much horizontal knockback to cancel (0-100%)
 * Vertical slider: how much vertical knockback to cancel (0-100%)
 *
 * At 100% horizontal + 100% vertical you barely move when hit.
 *
 * CATEGORY: Combat
 */
public class Velocity extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    public final Setting horizontal = new Setting("Horizontal", 100.0f, 0.0f, 100.0f);
    public final Setting vertical   = new Setting("Vertical",    50.0f, 0.0f, 100.0f);

    // Track previous velocity to detect sudden spikes (knockback)
    private double prevVX = 0, prevVZ = 0;
    private boolean firstTick = true;

    public Velocity() {
        super("Velocity", "Combat");
        settings.add(horizontal);
        settings.add(vertical);
        alwaysShowSettings = true;
    }

    @Override
    public void onEnable() {
        firstTick = true;
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;

        double vx = p.getDeltaMovement().x;
        double vy = p.getDeltaMovement().y;
        double vz = p.getDeltaMovement().z;

        if (firstTick) {
            prevVX = vx; prevVZ = vz;
            firstTick = false;
            return;
        }

        // Detect horizontal knockback: sudden large velocity increase
        double deltaH = Math.sqrt(
            Math.pow(vx - prevVX, 2) + Math.pow(vz - prevVZ, 2));

        if (deltaH > 0.15) {
            // This is a knockback spike — reduce it
            float hFactor = 1.0f - (horizontal.value / 100.0f);
            float vFactor = 1.0f - (vertical.value / 100.0f);

            double newVX = prevVX + (vx - prevVX) * hFactor;
            double newVY = vy > 0.2 ? prevVZ + (vy - 0.1) * vFactor : vy; // only cancel upward kb
            double newVZ = prevVZ + (vz - prevVZ) * hFactor;

            p.setDeltaMovement(newVX, newVY, newVZ);
        }

        prevVX = p.getDeltaMovement().x;
        prevVZ = p.getDeltaMovement().z;
    }
}
