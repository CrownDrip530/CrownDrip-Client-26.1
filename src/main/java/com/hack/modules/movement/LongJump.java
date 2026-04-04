package com.hack.modules.movement;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * LongJump — massive horizontal boost when you jump.
 * Detects rising edge of jump and applies velocity in facing direction.
 */
public class LongJump extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    public final Setting powerSetting = new Setting("Power", 3.0f, 1.0f, 10.0f);
    private boolean wasOnGround = false;

    public LongJump() {
        super("LongJump", "Movement");
        settings.add(powerSetting);
        alwaysShowSettings = true;
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;

        boolean onGround = p.isOnGround();

        // Detect jump (was on ground, now airborne, moving upward)
        if (wasOnGround && !onGround && p.getVelocity().y > 0) {
            float yaw = (float) Math.toRadians(p.getYaw());
            double boost = powerSetting.value;
            p.setVelocity(
                p.getVelocity().x - Math.sin(yaw) * boost,
                p.getVelocity().y,
                p.getVelocity().z + Math.cos(yaw) * boost
            );
        }

        wasOnGround = onGround;
    }

    @Override
    public void onDisable() { wasOnGround = false; }
}
