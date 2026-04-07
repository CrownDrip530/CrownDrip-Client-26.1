package com.hack.mixins;

import com.hack.HackClient;
import com.hack.modules.movement.Step;
import net.minecraft.world.entity.Entity;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * StepMixin — overrides getStepHeight() for the local player when Step is enabled.
 *
 * setStepHeight() was removed in 1.21.11. Instead we inject into getStepHeight()
 * and return our custom height when Step is active and this is the local player.
 */
@Mixin(Entity.class)
public class StepMixin {

    @Inject(method = "getStepHeight", at = @At("RETURN"), cancellable = true)
    private void onGetStepHeight(CallbackInfoReturnable<Float> cir) {
        if (HackClient.moduleManager == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Only override for local player
        if (((Object) this) != mc.player) return;

        Step step = HackClient.moduleManager.get(Step.class);
        if (step == null || !step.isEnabled()) return;

        cir.setReturnValue(step.heightSetting.value);
    }
}
