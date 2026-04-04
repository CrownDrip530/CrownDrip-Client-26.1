package com.hack.modules.combat;

import com.hack.HackClient;
import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * KillAura — auto attacks nearby entities.
 *
 * MultiHit mode: sends N attack packets per tick, bypassing the
 * vanilla cooldown. Works with all weapon enchantments (Fire Aspect,
 * Sharpness, Knockback etc) since the server applies them to each packet.
 *
 * MultiHit slider: how many hits to send per tick (1-10).
 * At 1 = normal speed. At 5 = 5x attack speed.
 * Combined with CPS slider for fine control.
 */
public class KillAura extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    private LivingEntity currentTarget = null;

    public final Setting rangeSetting    = new Setting("Range",    4.0f, 1.0f, 10.0f);
    public final Setting cpsSetting      = new Setting("CPS",      10.0f, 1.0f, 20.0f);
    public final Setting rotateSetting   = new Setting("Rotate",   1.0f, 0.0f, 1.0f);
    public final Setting multiHitSetting = new Setting("MultiHit", 1.0f, 1.0f, 10.0f);

    private int attackTimer = 0;

    public KillAura() {
        super("KillAura", "Combat");
        settings.add(rangeSetting);
        settings.add(cpsSetting);
        settings.add(rotateSetting);
        settings.add(multiHitSetting);
    }

    @Override
    public void onDisable() { attackTimer = 0; currentTarget = null; }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        int ticksPerAttack = Math.max(1, (int)(20.0f / cpsSetting.value));
        attackTimer++;
        if (attackTimer < ticksPerAttack) return;
        attackTimer = 0;

        float range = rangeSetting.value;
        Box searchBox = player.getBoundingBox().expand(range, range, range);

        AntiBot antiBot = HackClient.moduleManager == null ? null :
                HackClient.moduleManager.get(AntiBot.class);

        List<LivingEntity> targets = mc.level.getEntitiesByClass(
                LivingEntity.class, searchBox,
                e -> e != player && e.isAlive() && !e.isSpectator()
                     && player.distanceTo(e) <= range
                     && (antiBot == null || !antiBot.isBot(e))
        );

        if (targets.isEmpty()) return;
        targets.sort((a, b) -> Double.compare(player.distanceTo(a), player.distanceTo(b)));
        LivingEntity target = targets.get(0);

        if (rotateSetting.value >= 0.5f) rotateToward(player, target);

        Criticals crits = HackClient.moduleManager == null ? null :
                HackClient.moduleManager.get(Criticals.class);
        if (crits != null && crits.isEnabled()) crits.prepareCrit();

        currentTarget = target;

        // MultiHit: send N attack packets per tick
        // Each packet is processed independently by the server with full enchant effects
        int hits = Math.max(1, (int) multiHitSetting.value);
        for (int i = 0; i < hits; i++) {
            mc.interactionManager.attackEntity(player, target);
        }
        player.swingHand(InteractionHand.MAIN_HAND);
    }

    public LivingEntity getCurrentTarget() { return currentTarget; }

    private void rotateToward(LocalPlayer player, Entity target) {
        double dx = target.getX() - player.getX();
        double dy = (target.getY() + target.getHeight() / 2.0)
                  - (player.getY() + player.getEyeHeight(player.getPose()));
        double dz = target.getZ() - player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        player.setYaw((float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0));
        player.setPitch((float)(-Math.toDegrees(Math.atan2(dy, dist))));
    }
}
