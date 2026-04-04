package com.hack.modules.movement;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;

/**
 * SafeWalk — automatically sneaks when near block edges to prevent falling.
 */
public class SafeWalk extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    public SafeWalk() { super("SafeWalk", "Movement"); }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null || mc.world == null) return;
        if (!p.isOnGround()) return;

        // Check if there's a block below us in any direction
        boolean edgeDanger = false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                BlockPos check = p.getBlockPos().add(dx, -1, dz);
                if (mc.world.getBlockState(check).isAir()) {
                    edgeDanger = true; break;
                }
            }
            if (edgeDanger) break;
        }

        p.setSneaking(edgeDanger);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) mc.player.setSneaking(false);
    }
}
