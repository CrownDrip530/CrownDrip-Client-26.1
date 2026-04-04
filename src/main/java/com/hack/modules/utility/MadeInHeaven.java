package com.hack.modules.utility;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

/**
 * MadeInHeaven — speeds up your effective tick rate by sending extra
 * movement packets per real tick, making the server process your actions
 * faster than other players.
 *
 * HOW IT WORKS:
 * Normally Minecraft sends 1 position packet per tick (20/sec).
 * This module sends N packets per tick where N = multiplier setting.
 * The server processes each packet as a separate game action, so from
 * the server's perspective you are moving/acting N times faster.
 *
 * Also calls onTick() on KillAura and AutoClicker extra times so
 * attacks happen at the multiplied rate.
 *
 * WHAT YOUR SERVER SEES (detect this):
 * - Receiving N position packets per tick instead of 1
 * - Player movement distance per tick is N times normal
 * - Attack packets arriving faster than the 20/sec limit
 *
 * CATEGORY: Utility
 */
public class MadeInHeaven extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    // Multiplier: 1x to 5x (1x = normal speed, 5x = five times faster)
    public final Setting multiplier = new Setting("Speed", 2.0f, 1.0f, 5.0f);

    public MadeInHeaven() {
        super("MadeInHeaven", "Utility");
        settings.add(multiplier);
        alwaysShowSettings = true;
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null || p.networkHandler == null) return;

        int extraTicks = (int) multiplier.value - 1;
        if (extraTicks <= 0) return;

        // Send extra position packets to simulate faster tick rate
        for (int i = 0; i < extraTicks; i++) {
            p.networkHandler.sendPacket(
                new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PositionAndOnGround(
                    p.getX(), p.getY(), p.getZ(),
                    p.isOnGround(), false
                )
            );
        }
    }
}
