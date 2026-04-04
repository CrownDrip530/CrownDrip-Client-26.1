package com.hack.mixins;

import com.hack.HackClient;
import com.hack.modules.combat.HitboxExtend;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * HitboxMixin — inflates other players' bounding boxes for HitboxExtend.
 *
 * Injects at RETURN of Entity.getBoundingBox() and expands the returned
 * box by expandSetting.value in all 6 directions.
 *
 * Only applies to Player instances that are NOT the local player.
 * This means:
 * - Your own hitbox is unchanged (no sinking/collision issues)
 * - Enemy players get expanded boxes (easier to click/hit them)
 * - Mobs are unaffected
 *
 * Effect on gameplay:
 * - Client-side crosshair targeting: easier to click on far/moving targets
 * - KillAura entity detection: finds targets more easily
 * - Server-side hit detection: NOT affected (server uses its own boxes)
 */
@Mixin(Entity.class)
public class HitboxMixin {

    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void onGetBoundingBox(CallbackInfoReturnable<AABB> cir) {
        // Fast exit: skip if not a player entity
        if (!((Object)this instanceof Player)) return;

        if (HackClient.moduleManager == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Skip local player
        if (((Object)this) == mc.player) return;

        HitboxExtend hitbox = HackClient.moduleManager.get(HitboxExtend.class);
        if (hitbox == null || !hitbox.isEnabled()) return;

        float expand = hitbox.expandSetting.value;
        if (expand <= 0.0f) return;

        cir.setReturnValue(cir.getReturnValue().expand(expand));
    }
}
