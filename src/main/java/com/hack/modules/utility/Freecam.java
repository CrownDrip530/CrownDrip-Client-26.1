package com.hack.modules.utility;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * Freecam - Detaches your camera so it flies freely while your body stays still.
 *
 * HOW IT WORKS:
 * Creates a "spectator-like" camera mode client-side by:
 * 1. Stopping the player's body from sending movement packets (freezes body)
 * 2. Detaching the camera from the player entity
 * 3. Letting the camera move freely with WASD/Space/Shift
 *
 * Your body stays frozen at the original position on the server.
 * Other players see you standing still while you can look anywhere.
 *
 * WHAT YOUR SERVER SEES (detect this):
 * - Player position completely frozen for extended period
 * - Zero velocity while player was previously moving
 * - No position packet updates (or identical packets every tick)
 *
 * CATEGORY: Utility
 */
public class Freecam extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    // Camera position (moves freely)
    private double camX, camY, camZ;
    private float  camYaw, camPitch;
    private boolean initialized = false;

    public Freecam() {
        super("Freecam", "Utility");
    }

    @Override
    public void onEnable() {
        LocalPlayer p = mc.player;
        if (p == null) return;

        // Save player's current position as starting camera position
        camX     = p.getX();
        camY     = p.getY() + p.getEyeHeight(p.getPose());
        camZ     = p.getZ();
        camYaw   = p.getYRot();
        camPitch = p.getXRot();
        initialized = true;

        // Freeze the player body by zeroing velocity
        p.setDeltaMovement(0, 0, 0);
    }

    @Override
    public void onDisable() {
        initialized = false;
        // Player body snaps back to server position automatically
    }

    @Override
    public void onTick() {
        if (!isEnabled() || !initialized) return;
        LocalPlayer p = mc.player;
        if (p == null) return;

        // Freeze the player body — don't let it move
        p.setDeltaMovement(0, 0, 0);

        // Move camera based on look direction and WASD keys
        float speed = 0.3f;
        float yawRad = (float) Math.toRadians(camYaw);

        if (mc.options.keyUp.isPressed()) {
            camX -= Math.sin(yawRad) * speed;
            camZ += Math.cos(yawRad) * speed;
        }
        if (mc.options.keyDown.isPressed()) {
            camX += Math.sin(yawRad) * speed;
            camZ -= Math.cos(yawRad) * speed;
        }
        if (mc.options.keyLeft.isPressed()) {
            camX -= Math.cos(yawRad) * speed;
            camZ -= Math.sin(yawRad) * speed;
        }
        if (mc.options.keyRight.isPressed()) {
            camX += Math.cos(yawRad) * speed;
            camZ += Math.sin(yawRad) * speed;
        }
        if (mc.options.jumpKey.isPressed())  camY += speed;
        if (mc.options.sneakKey.isPressed()) camY -= speed;
    }

    // Getters for GameRendererMixin to use when rendering from camera position
    public double getCamX()    { return camX; }
    public double getCamY()    { return camY; }
    public double getCamZ()    { return camZ; }
    public float  getCamYaw()  { return camYaw; }
    public float  getCamPitch(){ return camPitch; }
    public void   setCamYaw(float y)   { this.camYaw   = y; }
    public void   setCamPitch(float p) { this.camPitch = p; }
    public boolean isInitialized() { return initialized; }
}
