package com.hack.modules.utility;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * ClickTP — right-click to teleport to where you're looking.
 * Checked every tick — when right-click pressed and looking at a block,
 * sends position packet to that location.
 */
public class ClickTP extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    public ClickTP() { super("ClickTP", "Utility"); }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        if (mc.player == null || mc.player.networkHandler == null) return;
        if (!mc.options.useKey.isPressed()) return;
        if (mc.crosshairTarget == null) return;
        if (mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult hit = (BlockHitResult) mc.crosshairTarget;
        double x = hit.getBlockPos().getX() + 0.5;
        double y = hit.getBlockPos().getY() + 1.0;
        double z = hit.getBlockPos().getZ() + 0.5;

        mc.player.refreshPositionAndAngles(x, y, z,
            mc.player.getYaw(), mc.player.getPitch());
        mc.player.networkHandler.sendPacket(
            new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PositionAndOnGround(x, y, z, true, false));
    }
}
