package com.hack.modules.utility;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/**
 * Scaffold — places blocks under feet automatically.
 *
 * 1.21.11 FIX:
 * Use getSelectedSlot() and setSelectedSlot() which were added in 1.21.x
 * instead of directly accessing the private selectedSlot field.
 * Also use createSlotSetPacket() to send the correct packet to the server.
 */
public class Scaffold extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    private int savedSlot = -1;

    public Scaffold() {
        super("Scaffold", "Utility");
    }

    @Override
    public void onDisable() {
        if (savedSlot != -1 && mc.player != null) {
            mc.player.getInventory().setSelectedSlot(savedSlot);
            mc.getConnection().sendPacket(
                new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(savedSlot));
            savedSlot = -1;
        }
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || mc.gameMode == null) return;

        BlockPos below = BlockPos.containing(p.getX(), p.getY() - 0.1, p.getZ());
        if (!mc.level.getBlockState(below).isAir()) return;

        // Find block in hotbar
        int blockSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = p.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                blockSlot = i;
                break;
            }
        }
        if (blockSlot == -1) return;

        // Save and switch slot
        savedSlot = p.getInventory().getSelectedSlot();
        p.getInventory().setSelectedSlot(blockSlot);
        mc.getConnection().sendPacket(new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(blockSlot));

        // Place block below
        BlockPos placeOn = below.below();
        Vec3 hitVec = new Vec3(below.getX() + 0.5, below.getY(), below.getZ() + 0.5);
        BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, placeOn, false);
        mc.gameMode.useItemOn(p, InteractionHand.MAIN_HAND, hitResult);
        p.swing(InteractionHand.MAIN_HAND);

        // Restore slot immediately
        p.getInventory().setSelectedSlot(savedSlot);
        mc.getConnection().sendPacket(new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(savedSlot));
        savedSlot = -1;
    }
}
