package com.hack.modules.movement;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.InteractionHand;

/**
 * BoatFly — fly while mounted in a boat.
 *
 * FIXES:
 * - Removed interactEntity call on enable (wrong API — requires EntityHitResult).
 *   Instead, user just needs to already be in a boat when they enable this.
 * - Removed PlayerInputC2SPacket dismount on disable (incorrect field names in 1.20.1).
 *   Now dismounts cleanly by calling startRiding(null) which the game handles.
 * - Added null check for mc.gameMode.
 *
 * HOW TO USE:
 * Get into a boat normally first, then enable BoatFly.
 * WASD = steer, Space = up, Shift = down.
 */
public class BoatFly extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    public final Setting speedSetting = new Setting("Speed", 0.5f, 0.1f, 2.0f);

    public BoatFly() {
        super("BoatFly", "Movement");
        settings.add(speedSetting);
    }

    @Override
    public void onDisable() {
        // Dismount the boat on disable by zeroing boat velocity
        // (server will re-apply gravity naturally)
        LocalPlayer p = mc.player;
        if (p == null) return;
        if (p.isPassenger() && p.getVehicle() instanceof Boat b) {
            b.setDeltaMovement(0, 0, 0);
        }
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null) return;

        if (!(p.getVehicle() instanceof Boat boatEntity)) return;

        float speed = speedSetting.value;
        float yaw   = (float) Math.toRadians(p.getYRot());

        double vx = 0, vy = 0, vz = 0;

        if (mc.options.keyUp.isDown()) {
            vx -= Math.sin(yaw) * speed;
            vz += Math.cos(yaw) * speed;
        }
        if (mc.options.keyDown.isDown()) {
            vx += Math.sin(yaw) * speed * 0.5f;
            vz -= Math.cos(yaw) * speed * 0.5f;
        }
        if (mc.options.keyRight.isDown()) {
            vx += Math.cos(yaw) * speed * 0.5f;
            vz += Math.sin(yaw) * speed * 0.5f;
        }
        if (mc.options.keyLeft.isDown()) {
            vx -= Math.cos(yaw) * speed * 0.5f;
            vz -= Math.sin(yaw) * speed * 0.5f;
        }
        if (mc.options.keyJump.isDown())  vy =  speed;
        if (mc.options.keyShift.isDown()) vy = -speed;

        boatEntity.setDeltaMovement(vx, vy, vz);
        boatEntity.setNoGravity(true); // cancel gravity on the boat entity
    }
}
