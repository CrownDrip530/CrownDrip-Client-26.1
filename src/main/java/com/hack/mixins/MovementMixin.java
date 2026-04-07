package com.hack.mixins;

import com.hack.HackClient;
import com.hack.modules.movement.FlyHack;
import com.hack.modules.movement.NoFall;
import com.hack.modules.utility.NoClip;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MovementMixin - Handles FlyHack and NoFall packet manipulation.
 */
@Mixin(LocalPlayer.class)
public class MovementMixin {

    @Inject(method = "sendPosition", at = @At("HEAD"))
    private void beforeMovementPacket(CallbackInfo ci) {
        if (HackClient.moduleManager == null) return;

        LocalPlayer player = (LocalPlayer)(Object) this;

        // NoFall: force onGround=true in every packet so server never registers a fall
        NoFall noFall = HackClient.moduleManager.get(NoFall.class);
        if (noFall != null && noFall.isEnabled()) {
            player.setOnGround(true);
            player.fallDistance = 0.0f;
        }

        // FlyHack: cancel gravity and fake ground
        FlyHack flyHack = HackClient.moduleManager.get(FlyHack.class);
        if (flyHack != null && flyHack.isEnabled()) {
            player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
            player.setOnGround(true);
        }

        // NoClip: fake onGround=true so server doesn't kick for floating
        NoClip noClip = HackClient.moduleManager.get(NoClip.class);
        if (noClip != null && noClip.isEnabled()) {
            player.setOnGround(true);
        }
    }
}
