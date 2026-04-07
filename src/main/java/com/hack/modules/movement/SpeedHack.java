package com.hack.modules.movement;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * SpeedHack - Run faster than normal.
 *
 * HOW IT WORKS in 1.21.11:
 * Every tick, if the player is pressing movement keys, we directly set
 * their velocity to the target speed in the direction they're facing.
 * This overrides friction-based slowdown and gives consistent fast movement.
 */
public class SpeedHack extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    public final Setting speedSetting = new Setting("Speed", 0.5f, 0.1f, 2.0f);

    public SpeedHack() {
        super("Speed", "Movement");
        settings.add(speedSetting);
        alwaysShowSettings = true;
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;

        boolean forward  = mc.options.keyUp.isDown();
        boolean backward = mc.options.keyDown.isDown();
        boolean left     = mc.options.keyLeft.isDown();
        boolean right    = mc.options.keyRight.isDown();

        if (!forward && !backward && !left && !right) return;

        float speed = speedSetting.value;
        float yaw   = (float) Math.toRadians(p.getYRot());

        double vx = 0, vz = 0;

        if (forward)  { vx -= Math.sin(yaw) * speed; vz += Math.cos(yaw) * speed; }
        if (backward) { vx += Math.sin(yaw) * speed; vz -= Math.cos(yaw) * speed; }
        if (left)     { vx -= Math.cos(yaw) * speed; vz -= Math.sin(yaw) * speed; }
        if (right)    { vx += Math.cos(yaw) * speed; vz += Math.sin(yaw) * speed; }

        // Normalize diagonal movement
        double len = Math.sqrt(vx * vx + vz * vz);
        if (len > speed) { vx = vx / len * speed; vz = vz / len * speed; }

        p.setDeltaMovement(vx, p.getDeltaMovement().y, vz);
    }
}
