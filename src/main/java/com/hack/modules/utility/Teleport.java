package com.hack.modules.utility;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;

/**
 * Teleport - Instantly moves the player to a set X Y Z coordinate.
 *
 * FIX: Removed onEnable() which caused an infinite loop:
 *   toggle() → onEnable() → setEnabledSilently() → toggle() → onEnable() → ...
 *
 * Now the Teleport module is purely passive — it just holds coordinates
 * and provides executeTeleport(). Opening the GUI is handled entirely
 * by ClickGUI.clickRow() when the user left-clicks the Teleport row.
 *
 * If the user binds a key to Teleport via .bind, pressing that key
 * will just toggle enabled on/off (no GUI) — which is harmless.
 */
public class Teleport extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    public double targetX = 0;
    public double targetY = 64;
    public double targetZ = 0;

    public Teleport() {
        super("Teleport", "Utility");
    }

    /**
     * Pre-fills coordinates with the player's current position.
     * Called by ClickGUI just before opening TeleportGUI.
     */
    public void prefillCurrentPosition() {
        if (mc.player == null) return;
        targetX = mc.player.getX();
        targetY = mc.player.getY();
        targetZ = mc.player.getZ();
    }

    /**
     * Called by TeleportGUI when the Teleport button is clicked.
     * Moves the player instantly and sends a position packet to the server.
     */
    public void executeTeleport() {
        if (mc.player == null) return;
        if (mc.player.networkHandler == null) return;

        try {
            // refreshPositionAndAngles properly updates all internal position state
            mc.player.refreshPositionAndAngles(targetX, targetY, targetZ,
                mc.player.getYRot(), mc.player.getPitch());

            // Send position packet to server
            mc.player.networkHandler.sendPacket(
                new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PositionAndOnGround(
                    targetX, targetY, targetZ, true, false
                )
            );
        } catch (Exception e) {
            System.out.println("[CrownDrip] Teleport error: " + e.getMessage());
        }
    }
}
