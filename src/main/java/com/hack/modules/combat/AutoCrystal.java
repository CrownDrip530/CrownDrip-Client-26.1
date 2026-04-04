package com.hack.modules.combat;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * AutoCrystal — automatically places and explodes end crystals on enemies.
 *
 * HOW IT WORKS:
 * 1. Find the nearest enemy player
 * 2. Find the best block adjacent to them to place a crystal on (obsidian/bedrock)
 * 3. Switch to end crystals in hotbar
 * 4. Place crystal on that block
 * 5. Immediately attack (explode) the crystal
 * 6. Repeat every tick
 *
 * VERY powerful — can kill players in full netherite quickly.
 * Needs end crystals in hotbar to work.
 */
public class AutoCrystal extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    public final Setting rangeSetting  = new Setting("Range",  6.0f, 3.0f, 10.0f);
    public final Setting delaySetting  = new Setting("Delay",  1.0f, 0.0f,  5.0f);

    private int tickDelay = 0;
    private int savedSlot = -1;

    public AutoCrystal() {
        super("AutoCrystal", "Combat");
        settings.add(rangeSetting);
        settings.add(delaySetting);
        alwaysShowSettings = true;
    }

    @Override
    public void onDisable() {
        restoreSlot();
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null || mc.level == null || mc.interactionManager == null) return;

        if (tickDelay > 0) { tickDelay--; return; }

        // Step 1: Explode any existing crystals near enemies first
        for (EndCrystal crystal : mc.level.getEntitiesByClass(
                EndCrystal.class,
                p.getBoundingBox().expand(rangeSetting.value),
                e -> true)) {
            if (p.distanceTo(crystal) <= rangeSetting.value) {
                mc.interactionManager.attackEntity(p, crystal);
                p.swingHand(InteractionHand.MAIN_HAND);
            }
        }

        // Step 2: Find nearest enemy
        Player target = null;
        double closest = rangeSetting.value;
        for (Player player : mc.level.getPlayers()) {
            if (player == p) continue;
            double dist = p.distanceTo(player);
            if (dist < closest) { closest = dist; target = player; }
        }
        if (target == null) { restoreSlot(); return; }

        // Step 3: Find crystal in hotbar
        int crystalSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (p.getInventory().getStack(i).getItem() == Items.END_CRYSTAL) {
                crystalSlot = i; break;
            }
        }
        if (crystalSlot == -1) { restoreSlot(); return; }

        // Step 4: Find best placement position (block below or beside target)
        BlockPos targetPos = target.getBlockPos();
        BlockPos[] candidates = {
            targetPos.down(), targetPos.north(), targetPos.south(),
            targetPos.east(), targetPos.west()
        };

        BlockPos placePos = null;
        for (BlockPos candidate : candidates) {
            if (!mc.level.getBlockState(candidate).isAir()
                && mc.level.getBlockState(candidate.up()).isAir()
                && p.squaredDistanceTo(Vec3.ofCenter(candidate)) <= rangeSetting.value * rangeSetting.value) {
                placePos = candidate;
                break;
            }
        }
        if (placePos == null) { restoreSlot(); return; }

        // Step 5: Switch to crystal slot
        if (savedSlot == -1) savedSlot = p.getInventory().getSelectedSlot();
        p.getInventory().setSelectedSlot(crystalSlot);
        p.networkHandler.sendPacket(p.getInventory().createSlotSetPacket(crystalSlot));

        // Step 6: Place crystal
        Vec3 hitVec = Vec3.ofCenter(placePos).add(0, 0.5, 0);
        BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, placePos, false);
        mc.interactionManager.interactBlock(p, InteractionHand.MAIN_HAND, hitResult);
        p.swingHand(InteractionHand.MAIN_HAND);

        tickDelay = (int) delaySetting.value;
    }

    private void restoreSlot() {
        if (savedSlot != -1 && mc.player != null) {
            mc.player.getInventory().setSelectedSlot(savedSlot);
            mc.player.networkHandler.sendPacket(
                mc.player.getInventory().createSlotSetPacket(savedSlot));
            savedSlot = -1;
        }
    }
}
