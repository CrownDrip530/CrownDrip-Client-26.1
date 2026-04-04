package com.hack.modules.utility;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

/**
 * Disabler — spams specific packets to confuse or crash weak anticheats.
 *
 * HOW IT WORKS:
 * Many old/simple anticheats (NCP, basic custom ACs) have bugs where:
 * 1. Receiving too many movement packets per tick causes them to freeze
 *    their violation tracking (they can't process fast enough)
 * 2. Certain packet sequences cause the AC's state machine to reset,
 *    clearing accumulated violation levels
 * 3. Flooding with onGround=true/false alternating confuses ground checks
 *
 * MODES:
 *  0 = Packet flood: sends extra position packets rapidly this tick
 *  1 = Ground flicker: rapidly alternates onGround true/false
 *  2 = Timer: speeds up packet send rate slightly to desync AC timers
 *
 * IMPORTANT: This is increasingly ineffective against modern anticheats
 * (Grim, Spartan, Vulcan) which are designed to handle packet floods.
 * Works best against NCP, AAC, and custom basic ACs on smaller servers.
 *
 * WHAT YOUR SERVER SEES (detect this):
 * - Multiple position packets arriving in the same tick
 * - onGround flipping every packet with no movement
 * - Packet arrival rate faster than 20/sec
 *
 * CATEGORY: Utility
 */
public class Disabler extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    public final Setting modeSetting  = new Setting("Mode",   0.0f, 0.0f, 2.0f);
    public final Setting intensitySetting = new Setting("Intensity", 5.0f, 1.0f, 20.0f);

    public Disabler() {
        super("Disabler", "Utility");
        settings.add(modeSetting);
        settings.add(intensitySetting);
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;

        int mode      = (int) modeSetting.value;
        int intensity = (int) intensitySetting.value;

        switch (mode) {
            case 0:
                // Packet flood: send extra position packets
                for (int i = 0; i < intensity; i++) {
                    p.networkHandler.sendPacket(
                        new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PositionAndOnGround(
                            p.getX(), p.getY(), p.getZ(), p.onGround(), false
                        )
                    );
                }
                break;

            case 1:
                // Ground flicker: alternate onGround rapidly
                for (int i = 0; i < intensity; i++) {
                    boolean fakeGround = (i % 2 == 0);
                    p.networkHandler.sendPacket(
                        new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PositionAndOnGround(
                            p.getX(), p.getY(), p.getZ(), fakeGround, false
                        )
                    );
                }
                break;

            case 2:
                // Timer manipulation: send a position packet slightly
                // out of the normal 20-tick cycle to desync AC timers
                // (just one extra packet — subtle)
                p.networkHandler.sendPacket(
                    new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.OnGroundOnly(p.onGround(), false)
                );
                break;
        }
    }
}
