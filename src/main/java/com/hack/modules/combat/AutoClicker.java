package com.hack.modules.combat;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;

/**
 * AutoClicker - Automatically left-clicks at a set CPS (clicks per second).
 *
 * HOW IT WORKS:
 * Every tick, checks if enough time has passed based on the CPS setting.
 * If yes, triggers mc.doAttack() which is the same method called when
 * the player manually left-clicks — sends a UseEntity attack packet.
 *
 * Also adds small random variance to the timing to mimic human clicking
 * patterns and avoid the perfectly-consistent CPS detection.
 *
 * WHAT YOUR SERVER SEES (detect this):
 * - Attack packets at a rate exceeding human capability (>16 CPS sustained)
 * - Perfectly consistent timing between attacks (no variance)
 * - Attacks while no mouse input is registered (with client-side AC)
 *
 * CATEGORY: Combat
 */
public class AutoClicker extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    public final Setting cpsSetting = new Setting("CPS", 10.0f, 1.0f, 20.0f);

    private int tickCounter = 0;
    private int nextClickAt = 0;

    public AutoClicker() {
        super("AutoClicker", "Combat");
        settings.add(cpsSetting);
    }

    @Override
    public void onEnable() {
        tickCounter = 0;
        scheduleNextClick();
    }

    @Override
    public void onDisable() {
        tickCounter = 0;
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        if (mc.player == null) return;

        tickCounter++;

        if (tickCounter >= nextClickAt) {
            // Perform the attack (same as left-clicking)
            // Trigger Criticals before attacking
            com.hack.modules.combat.Criticals crits =
                com.hack.HackClient.moduleManager != null
                ? com.hack.HackClient.moduleManager.get(com.hack.modules.combat.Criticals.class)
                : null;
            if (crits != null && crits.isEnabled()) crits.prepareCrit();

            // Attack the currently targeted entity via interactionManager
            if (mc.targetedEntity != null && mc.interactionManager != null) {
                mc.interactionManager.attackEntity(mc.player, mc.targetedEntity);
                mc.player.swingHand(net.minecraft.world.InteractionInteractionHand.MAIN_HAND);
            }
            tickCounter = 0;
            scheduleNextClick();
        }
    }

    /**
     * Calculates when the next click should happen.
     * Adds small random variance (±1 tick) to look more human.
     */
    private void scheduleNextClick() {
        int baseTicks = Math.max(1, (int)(20.0f / cpsSetting.value));
        // Random variance of ±1 tick
        int variance = (int)(Math.random() * 3) - 1;
        nextClickAt = Math.max(1, baseTicks + variance);
    }
}
