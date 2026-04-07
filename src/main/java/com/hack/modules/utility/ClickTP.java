package com.hack.modules.utility;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
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
        if (mc.player == null || mc.getConnection() == null) return;
        if (!mc.options.keyUse.isDown()) return;
        if (mc.hitResult == null) return;
        if (mc.hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult hit = (BlockHitResult) mc.hitResult;
        double x = hit.getBlockPos().getX() + 0.5;
        double y = hit.getBlockPos().getY() + 1.0;
        double z = hit.getBlockPos().getZ() + 0.5;

        mc.player.refreshPositionAndAngles(x, y, z,
            mc.player.getYRot(), mc.player.getXRot());
        mc.getConnection().sendPacket(
            new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot(x, y, z, true, false));
    }
}
