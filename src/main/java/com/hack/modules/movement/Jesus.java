package com.hack.modules.movement;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

/**
 * Jesus - Walk on water (and lava).
 *
 * HOW IT WORKS:
 * When the player is in or on water, rapidly alternates Y position between
 * just above and just inside the water surface each tick (bounce exploit).
 * This exploits the fact that Minecraft treats the water surface as
 * semi-solid momentarily when the player is at the boundary.
 *
 * The client sends Y positions that stay at the water surface level,
 * and the server doesn't apply fluid drag because the player keeps
 * "touching" the surface boundary.
 *
 * WHAT YOUR SERVER SEES (detect this):
 * - Player Y stays exactly at water surface level for extended time
 * - No fluid physics applied (no drag, no bobbing)
 * - Perfect Y oscillation pattern (every 2 ticks)
 *
 * CATEGORY: Movement
 */
public class Jesus extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    private int bounceTimer = 0;

    public Jesus() {
        super("Jesus", "Movement");
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;

        // Activate when in water/lava OR when just above water surface (within 0.5 blocks)
        boolean inFluid = p.isInWater() || p.isInLava();
        boolean aboveWater = false;
        if (!inFluid) {
            // Check if there's water just below us
            net.minecraft.core.BlockPos below = net.minecraft.core.BlockPos.ofFloored(
                p.getX(), p.getY() - 0.5, p.getZ());
            var blockBelow = mc.level != null ? mc.level.getBlockState(below).getBlock() : null;
            aboveWater = blockBelow == net.minecraft.world.level.block.Blocks.WATER ||
                         blockBelow == net.minecraft.world.level.block.Blocks.LAVA;
        }
        if (!inFluid && !aboveWater) return;

        bounceTimer++;

        // Alternate: push up on even ticks, let gravity work on odd ticks
        // This creates the bounce that exploits the surface detection
        if (bounceTimer % 2 == 0) {
            p.setDeltaMovement(p.getDeltaMovement().x, 0.1, p.getDeltaMovement().z);
        } else {
            p.setDeltaMovement(p.getDeltaMovement().x, -0.1, p.getDeltaMovement().z);
        }

        // Fake being on ground so server doesn't apply fluid physics
        p.setOnGround(true);
    }

    @Override
    public void onDisable() {
        bounceTimer = 0;
    }
}
