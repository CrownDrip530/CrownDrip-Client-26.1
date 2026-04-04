package com.hack.modules.movement;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * FlyHack - Toggle-only via GUI or keybind set with .bind
 *
 * HOW IT WORKS:
 * Cancels gravity (motionY = 0) every tick before the position packet is sent.
 * Fakes onGround=true so basic fly checks don't trigger.
 * AntiKick: dips Y by -0.04 every 20 ticks to avoid "frozen Y" kick.
 *
 * WHAT YOUR SERVER SEES (detect this):
 * - Y position frozen 5+ ticks while airborne, no block beneath
 * - onGround=true but no solid block at feet
 * - Y dips exactly -0.04 every exactly 20 ticks (antikick pattern)
 * - abilities.isFlying=true on a survival player
 *
 * CATEGORY: Movement
 * DEFAULT KEYBIND: none (use .bind Fly [key] in chat)
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
        p.getAbilities().flying      = true;
        p.getAbilities().mayfly = true;
    }

    @Override
    public void onDisable() {
        LocalPlayer p = mc.player;
        if (p == null) return;
        p.getAbilities().flying      = false;
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

        // Cancel gravity
        p.setDeltaMovement(p.getDeltaMovement().x, 0, p.getDeltaMovement().z);

        if (mc.options.jumpKey.isPressed())
            p.setDeltaMovement(p.getDeltaMovement().x,  spd, p.getDeltaMovement().z);
        if (mc.options.sneakKey.isPressed())
            p.setDeltaMovement(p.getDeltaMovement().x, -spd, p.getDeltaMovement().z);

        // AntiKick: tiny Y dip every 20 ticks
        // SERVER: flag rhythmic -0.04 dip every ~20 ticks = antikick signature
        antiKickTimer++;
        if (antiKickTimer >= 20) {
            p.setDeltaMovement(p.getDeltaMovement().x, -0.04, p.getDeltaMovement().z);
            antiKickTimer = 0;
        }

        p.getAbilities().flying = true;
    }
}
