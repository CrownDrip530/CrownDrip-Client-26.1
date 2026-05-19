package com.hack.modules.utility;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

/**
 * Teleport - Instantly moves the player to a set X Y Z coordinate.
 */
public class Teleport extends HackModule {
    private final Minecraft mc = Minecraft.getInstance();
    public double targetX = 0;
    public double targetY = 64;
    public double targetZ = 0;

    public Teleport() {
        super("Teleport", "Utility");
    }

    public void prefillCurrentPosition() {
        if (mc.player == null) return;
        targetX = mc.player.getX();
        targetY = mc.player.getY();
        targetZ = mc.player.getZ();
    }

    public void executeTeleport() {
        if (mc.player == null) return;
        if (mc.getConnection() == null) return;
        try {
            // In 1.21.1 (26.1), LocalPlayer uses moveTo() instead of absMoveTo()
            mc.player.moveTo(targetX, targetY, targetZ,
                mc.player.getYRot(), mc.player.getXRot());
            
            mc.getConnection().send(
                new ServerboundMovePlayerPacket.PosRot(
                    targetX, targetY, targetZ, mc.player.getYRot(), mc.player.getXRot(), true
                )
            );
        } catch (Exception e) {
            System.out.println("[CrownDrip] Teleport error: " + e.getMessage());
        }
    }
}
