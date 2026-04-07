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
        if (!mc.options.keyUp.isDown() && !mc.options.keyDown.isDown()
            && !mc.options.keyLeft.isDown() && !mc.options.keyRight.isDown()) return;
        if (p.onGround()) p.jumpFromGround();
    }
}
