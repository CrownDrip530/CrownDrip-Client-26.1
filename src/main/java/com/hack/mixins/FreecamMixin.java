package com.hack.mixins;

import com.hack.HackClient;
import com.hack.modules.utility.Freecam;
import net.minecraft.client.Camera;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.entity.Entity;
import com.hack.mixins.CameraAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * FreecamMixin — overrides camera position when Freecam is active.
 *
 * Injects at HEAD of Camera.update() and if Freecam is enabled,
 * sets the camera position to the freecam coordinates and cancels
 * the vanilla camera update entirely.
 */
@Mixin(Camera.class)
public class FreecamMixin {

    @Inject(method = "update(Lnet/minecraft/world/BlockGetter;Lnet/minecraft/entity/Entity;ZZF)V", at = @At("HEAD"), cancellable = true)
    private void onCameraUpdate(BlockGetter area, Entity focusedEntity,
                                 boolean thirdPerson, boolean inverseView,
                                 float tickDelta, CallbackInfo ci) {
        if (HackClient.moduleManager == null) return;

        Freecam freecam = HackClient.moduleManager.get(Freecam.class);
        if (freecam == null || !freecam.isEnabled() || !freecam.isInitialized()) return;

        Camera camera = (Camera)(Object) this;

        // Set camera to freecam position and rotation
        ((CameraAccessor) camera).invokeSetPos(freecam.getCamX(), freecam.getCamY(), freecam.getCamZ());
        ((CameraAccessor) camera).invokeSetRotation(freecam.getCamYaw(), freecam.getCamPitch());

        // Cancel vanilla update so camera stays at our position
        ci.cancel();
    }
}
