package com.hack.modules.movement;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * FlyHack - Toggle-only via GUI or keybind set with .bind
 */
public class FlyHack extends HackModule {
    private final Minecraft mc = Minecraft.getInstance();
    private int antiKickTimer = 0;
    public final Setting speedSetting = new Setting("Speed", 0.15f, 0.05f, 1.0f);

    public FlyHack() {
        super("Fly", "Movement");
        settings.add(speedSetting);
        alwaysShowSettings = true;
    }

    @Override
    public void onEnable() {
        LocalPlayer p = mc.player;
        if (p == null) return;
        p.getAbilities().flying = true;
        p.getAbilities().mayfly = true;
    }

    @Override
    public void onDisable() {
        LocalPlayer p = mc.player;
        if (p == null) return;
        p.getAbilities().flying = false;
        p.getAbilities().mayfly = false;
        p.setDeltaMovement(p.getDeltaMovement().x, 0, p.getDeltaMovement().z);
        antiKickTimer = 0;
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;

        float spd = speedSetting.value;
        p.setDeltaMovement(p.getDeltaMovement().x, 0, p.getDeltaMovement().z);

        if (mc.options.keyJump.isDown())
            p.setDeltaMovement(p.getDeltaMovement().x,  spd, p.getDeltaMovement().z);
        if (mc.options.keyShift.isDown())
            p.setDeltaMovement(p.getDeltaMovement().x, -spd, p.getDeltaMovement().z);

        antiKickTimer++;
        if (antiKickTimer >= 20) {
            p.setDeltaMovement(p.getDeltaMovement().x, -0.04, p.getDeltaMovement().z);
            antiKickTimer = 0;
        }
        p.getAbilities().flying = true;
    }
}
