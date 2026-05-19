package com.hack.mixins;

import com.hack.HackClient;
import com.hack.modules.utility.AntiLagBack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * AntiLagBackMixin — intercepts position correction packets from the server.
 */
@Mixin(ClientPacketListener.class)
public class AntiLagBackMixin {
    private double lastAllowedX = Double.NaN;
    private double lastAllowedY = Double.NaN;
    private double lastAllowedZ = Double.NaN;

    @Inject(
        method = "handleMovePlayer",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onPositionCorrection(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        if (HackClient.moduleManager == null) return;
        AntiLagBack alb = HackClient.moduleManager.get(AntiLagBack.class);
        if (alb == null || !alb.isEnabled()) {
            lastAllowedX = Double.NaN;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (alb.modeSetting.value >= 0.5f) {
            ci.cancel();
            return;
        }

        if (Double.isNaN(lastAllowedX)) {
            lastAllowedX = mc.player.getX();
            lastAllowedY = mc.player.getY();
            lastAllowedZ = mc.player.getZ();
            return;
        }

        double dx = mc.player.getX() - lastAllowedX;
        double dy = mc.player.getY() - lastAllowedY;
        double dz = mc.player.getZ() - lastAllowedZ;
        double playerMovedDist = Math.sqrt(dx*dx + dy*dy + dz*dz);
        float threshold = alb.threshSetting.value;

        if (playerMovedDist < threshold) {
            ci.cancel();
        } else {
            lastAllowedX = mc.player.getX();
            lastAllowedY = mc.player.getY();
            lastAllowedZ = mc.player.getZ();
        }
    }
}
