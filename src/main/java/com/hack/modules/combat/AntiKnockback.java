package com.hack.modules.combat;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * AntiKnockback - Reduces or removes knockback when hit.
 *
 * HOW IT WORKS:
 * Every tick after the player is hit, the game applies a velocity impulse
 * (knockback) to push the player away from the attacker.
 * This hack zeros out or reduces that velocity impulse immediately after
 * it's applied, so the player barely moves.
 *
 * The reduction setting at 1.0 = completely cancels knockback.
 * At 0.5 = halves it, etc.
 *
 * WHAT YOUR SERVER SEES (detect this):
 * - Player position barely changes after receiving a hit
 * - Velocity after hit is near-zero when it should be substantial
 * - Player appears "glued" to the ground during combat
 *
 * CATEGORY: Combat
 */
public class AntiKnockback extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    public final Setting strengthSetting = new Setting("Reduction", 1.0f, 0.0f, 1.0f);

    public AntiKnockback() {
        super("AntiKnockback", "Combat");
        settings.add(strengthSetting);
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;

        // If player has a velocity that looks like knockback (horizontal spike),
        // reduce it by the configured amount
        double xVel = p.getVelocity().x;
        double zVel = p.getVelocity().z;
        double horizontalSpeed = Math.sqrt(xVel * xVel + zVel * zVel);

        // Knockback threshold: normal sprint is ~0.28, knockback is usually > 0.35
        if (horizontalSpeed > 0.35) {
            float reduction = strengthSetting.value;
            p.setVelocity(
                xVel * (1.0 - reduction),
                p.getVelocity().y,
                zVel * (1.0 - reduction)
            );
        }
    }
}
