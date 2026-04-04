package com.hack.modules.combat;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * ReachHack - Extends attack range beyond the vanilla 3-block limit.
 *
 * 1.21.11 approach: run in onTick(), check if attack key is pressed,
 * find entities within our custom range, and attack them directly via
 * interactionManager.attackEntity(). This bypasses the vanilla reach check
 * entirely since we're calling the attack method directly.
 */
public class ReachHack extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    public final Setting reachSetting = new Setting("Reach", 6.0f, 3.0f, 100.0f);

    private int attackCooldown = 0;

    public ReachHack() {
        super("Reach", "Combat");
        settings.add(reachSetting);
        alwaysShowSettings = true;
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null || mc.world == null || mc.interactionManager == null) return;

        if (attackCooldown > 0) { attackCooldown--; return; }
        if (!mc.options.attackKey.isPressed()) return;

        float range = reachSetting.value;

        // Only act on targets BEYOND vanilla reach (3 blocks)
        // Vanilla handles targets within 3 blocks itself
        Entity vanillaTarget = mc.targetedEntity;
        if (vanillaTarget != null && p.distanceTo(vanillaTarget) <= 3.0) return;

        Box searchBox = p.getBoundingBox().expand(range, range, range);
        List<LivingEntity> targets = mc.world.getEntitiesByClass(
            LivingEntity.class, searchBox,
            e -> e != p && e.isAlive() && !e.isSpectator()
                 && p.distanceTo(e) <= range
        );

        if (!targets.isEmpty()) {
            targets.sort((a, b) -> Double.compare(p.distanceTo(a), p.distanceTo(b)));
            Entity target = targets.get(0);
            mc.interactionManager.attackEntity(p, target);
            p.swingHand(InteractionHand.MAIN_HAND);
            // 5 ticks = 4 attacks/sec, matches sword attack speed
            attackCooldown = 5;
        }
    }

    @Override
    public void onDisable() {
        attackCooldown = 0;
    }
}
