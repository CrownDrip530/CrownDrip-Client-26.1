package com.hack.mixins;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * GameRendererMixin — placeholder.
 * HUD rendering moved to InGameHudMixin.
 * ESP/Tracers rendering will be re-added once the RenderPipeline API is implemented.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render(Lnet/minecraft/client/render/DeltaTracker;Z)V",
            at = @At("RETURN"))
    private void onRender(DeltaTracker tickCounter, boolean tick, CallbackInfo ci) {
        // ESP rendering placeholder - re-implement with RenderPipeline API
    }
}
