package com.hack.modules.movement;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Spider - Climb up any wall like a spider.
 *
 * HOW IT WORKS:
 * When the player is touching a wall (horizontally colliding) and holding
 * the jump key, sets a positive Y velocity to climb upward.
 * Cancels gravity while wall contact exists so the player doesn't slide down.
 *
 * WHAT YOUR SERVER SEES (detect this):
 * - Player Y increases while not on ground and not flying
 * - No ladder/vine block at player position
 * - Upward velocity while horizontally colliding with a block
 *
 * CATEGORY: Movement
 */
public class Spider extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    public final Setting speedSetting = new Setting("ClimbSpeed", 0.2f, 0.1f, 1.0f);

    public Spider() {
        super("Spider", "Movement");
        settings.add(speedSetting);
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;

        // Check if player is horizontally colliding with something (touching a wall)
        if (p.horizontalCollision) {
            // Cancel gravity and climb upward
            p.setDeltaMovement(
                p.getDeltaMovement().x,
                speedSetting.value,
                p.getDeltaMovement().z
            );
        }
    }
}
