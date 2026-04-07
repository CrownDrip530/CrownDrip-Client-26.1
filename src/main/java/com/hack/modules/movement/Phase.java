package com.hack.modules.movement;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Phase — slip through walls by temporarily enabling noClip
 * when the player is colliding horizontally with a block.
 *
 * Smarter than NoClip: only phases when actually stuck against a wall,
 * normal physics otherwise. Harder to detect than full NoClip.
 */
public class Phase extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    public Phase() {
        super("Phase", "Movement");
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;

        // Enable noClip only when horizontally colliding (stuck against wall)
        if (p.horizontalCollision) {
            p.noPhysics = true;
            // Push through the wall slightly
            float yaw = (float) Math.toRadians(p.getYRot());
            p.setDeltaMovement(
                p.getDeltaMovement().x - Math.sin(yaw) * 0.1,
                p.getDeltaMovement().y,
                p.getDeltaMovement().z + Math.cos(yaw) * 0.1
            );
        } else {
            p.noPhysics = false;
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.noPhysics = false;
    }
}
