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
        if (p == null || mc.level == null || mc.gameMode == null) return;

        if (attackCooldown > 0) { attackCooldown--; return; }
        if (!mc.options.keyAttack.isDown()) return;

        float range = reachSetting.value;
        Entity vanillaTarget = mc.crosshairPickEntity;
        if (vanillaTarget != null && p.distanceTo(vanillaTarget) <= 3.0) return;

        AABB searchBox = p.getBoundingBox().inflate(range);
        List<LivingEntity> targets = mc.level.getEntitiesOfClass(
            LivingEntity.class, searchBox,
            e -> e != p && e.isAlive() && !e.isSpectator()
                 && p.distanceTo(e) <= range
        );

        if (!targets.isEmpty()) {
            targets.sort((a, b) -> Double.compare(p.distanceTo(a), p.distanceTo(b)));
            Entity target = targets.get(0);
            mc.gameMode.attack(p, target);
            p.swing(InteractionHand.MAIN_HAND);
            attackCooldown = 5;
        }
    }

    @Override
    public void onDisable() {
        attackCooldown = 0;
    }
}
