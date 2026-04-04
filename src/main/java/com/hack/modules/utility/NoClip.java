package com.hack.modules.utility;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * NoClip - Walk through solid blocks.
 *
 * HOW IT WORKS:
 * Sets the player's noClip flag to true every tick. When noClip is true,
 * the vanilla collision system skips block collision checks entirely,
 * letting the player move freely through any block.
 *
 * This also disables gravity locally (the player floats), so you control
 * movement fully with WASD + Space/Shift.
 *
 * WHAT YOUR SERVER SEES (detect this):
 * - Player position inside solid blocks (impossible normally)
 * - No collision events where there should be
 * - Position packets placing player inside terrain
 *
 * NOTE: Most servers will kick or rubberband you immediately
 * because server-side collision still applies. Works best in
 * singleplayer or on servers with weak anticheat.
 *
 * CATEGORY: Utility
 */
public class NoClip extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    public NoClip() {
        super("NoClip", "Utility");
    }

    @Override
    public void onEnable() {
        if (mc.player != null) mc.player.noPhysics = true;
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.noPhysics = false;
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;

        // Keep noClip true every tick (game resets it)
        p.noPhysics = true;

        // Cancel gravity so player floats freely
        p.setDeltaMovement(p.getDeltaMovement().x, 0, p.getDeltaMovement().z);

        // Space = up, Shift = down
        if (mc.options.jumpKey.isPressed())
            p.setDeltaMovement(p.getDeltaMovement().x, 0.2, p.getDeltaMovement().z);
        if (mc.options.sneakKey.isPressed())
            p.setDeltaMovement(p.getDeltaMovement().x, -0.2, p.getDeltaMovement().z);
    }
}
