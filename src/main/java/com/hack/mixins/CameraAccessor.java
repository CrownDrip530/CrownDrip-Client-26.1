package com.hack.mixins;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * CameraAccessor — exposes protected Camera methods for FreecamMixin.
 */
@Mixin(Camera.class)
public interface CameraAccessor {

    @Invoker("setPos")
    void invokeSetPos(double x, double y, double z);

    @Invoker("setRotation")
    void invokeSetRotation(float yaw, float pitch);
}
