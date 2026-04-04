package com.hack.modules.movement;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * BunnyHop — auto-jumps at perfect timing to maintain sprint speed.
 * Makes you very hard to catch in PvP.
 */
public class BunnyHop extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    public BunnyHop() { super("BunnyHop", "Movement"); }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;
        if (!mc.options.keyUp.isPressed() && !mc.options.keyDown.isPressed()
            && !mc.options.keyLeft.isPressed() && !mc.options.keyRight.isPressed()) return;
        if (p.onGround()) p.jump();
    }
}
