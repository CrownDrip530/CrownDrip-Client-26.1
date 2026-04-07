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
 *
 * In 1.21.11 ClientboundPlayerPositionPacket is a Java record.
 * The pos() accessor returns a wrapped position object.
 * Rather than trying to extract exact coords (API varies by version),
 * we use a reliable hybrid approach:
 *
 * Smart mode: track the player's last known good position ourselves.
 * If we haven't moved much since our last good tick, the correction
 * is probably rubberbanding (server didn't like our movement) — block it.
 * If we teleported/spawned/portalled (large natural movement), allow it.
 *
 * Block-all mode: cancel every single position correction unconditionally.
 */
@Mixin(net.minecraft.client.multiplayer.ClientPacketListener.class)
public class AntiLagBackMixin {

    // Track last position we were at when we allowed a correction through
    private double lastAllowedX = Double.NaN;
    private double lastAllowedY = Double.NaN;
    private double lastAllowedZ = Double.NaN;

    @Inject(
        method = "onPlayerPositionLook",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onPositionCorrection(ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        if (HackClient.moduleManager == null) return;

        AntiLagBack alb = HackClient.moduleManager.get(AntiLagBack.class);
        if (alb == null || !alb.isEnabled()) {
            // Module off — reset our tracking so next enable starts fresh
            lastAllowedX = Double.NaN;
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Block-all mode: cancel every correction
        if (alb.modeSetting.value >= 0.5f) {
            ci.cancel();
            return;
        }

        // Smart mode: allow the first correction after enable (sets baseline)
        if (Double.isNaN(lastAllowedX)) {
            lastAllowedX = mc.player.getX();
            lastAllowedY = mc.player.getY();
            lastAllowedZ = mc.player.getZ();
            return; // Allow this one through
        }

        // Check how far we've moved since the last allowed correction
        // If we haven't moved much legitimately, the server is rubberbanding us
        double dx = mc.player.getX() - lastAllowedX;
        double dy = mc.player.getY() - lastAllowedY;
        double dz = mc.player.getZ() - lastAllowedZ;
        double playerMovedDist = Math.sqrt(dx*dx + dy*dy + dz*dz);

        float threshold = alb.threshSetting.value;

        if (playerMovedDist < threshold) {
            // We haven't moved far from our last good position —
            // this correction is server rubberbanding — block it
            ci.cancel();
        } else {
            // We moved a lot legitimately (portal, spawn, etc) — allow it
            lastAllowedX = mc.player.getX();
            lastAllowedY = mc.player.getY();
            lastAllowedZ = mc.player.getZ();
        }
    }
}
