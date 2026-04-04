package com.hack.modules.utility;

import com.hack.modules.HackModule;

/**
 * AntiLagBack — prevents the server from force-correcting your position.
 *
 * HOW IT WORKS:
 * Servers send a ClientboundPlayerPositionPacket to rubberbandyou back when
 * they detect illegal movement (fly, speed, teleport etc).
 * A mixin on ClientPacketListener.onPlayerPositionLook() intercepts
 * this packet before it's applied.
 *
 * TWO MODES (via slider):
 *
 * Mode 0 — Smart (default, recommended):
 *   Checks how far the server wants to move you.
 *   If the correction is small (< threshold blocks) it's probably
 *   a legitimate correction (spawn, portal, etc) — allow it.
 *   If the correction is large (> threshold blocks) it's rubberbanding
 *   from anticheat — block it.
 *   Threshold slider controls the cutoff distance (default 8 blocks).
 *
 * Mode 1 — Block All:
 *   Cancels every single position correction from the server.
 *   Much more aggressive. You will desync completely over time
 *   and eventually get kicked, but useful for short bursts.
 *
 * WHAT YOUR SERVER SEES (detect this):
 * - Position correction packet sent but player position doesn't change
 * - Player continues moving after correction should have applied
 * - No teleport acknowledgement sent back (some servers check this)
 *
 * CATEGORY: Utility
 */
public class AntiLagBack extends HackModule {

    // Mode: 0 = smart (distance-based), 1 = block all
    public final Setting modeSetting      = new Setting("Mode",      0.0f, 0.0f, 1.0f);
    // Threshold: corrections larger than this (in blocks) get blocked in smart mode
    public final Setting threshSetting    = new Setting("Threshold", 8.0f, 1.0f, 64.0f);

    public AntiLagBack() {
        super("AntiLagBack", "Utility");
        settings.add(modeSetting);
        settings.add(threshSetting);
    }

    /**
     * Called by AntiLagBackMixin when a position correction arrives.
     * Returns true if the correction should be BLOCKED (not applied).
     *
     * @param newX, newY, newZ  position the server wants to move us to
     * @param currentX, currentY, currentZ  our current position
     */
    public boolean shouldBlock(double newX, double newY, double newZ,
                                double currentX, double currentY, double currentZ) {
        if (!isEnabled()) return false;

        if (modeSetting.value >= 0.5f) {
            // Mode 1: block everything
            return true;
        }

        // Mode 0: smart — block only large corrections (rubberbanding)
        double dx = newX - currentX;
        double dy = newY - currentY;
        double dz = newZ - currentZ;
        double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);

        return dist > threshSetting.value;
    }
}
