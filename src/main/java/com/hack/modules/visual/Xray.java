package com.hack.modules.visual;

import com.hack.modules.HackModule;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Xray — highlights ores through terrain via screen-space dots.
 * Uses CopyOnWriteArrayList to prevent ConcurrentModificationException
 * between onTick (game thread) and renderHud (render thread).
 */
public class Xray extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    public final Setting rangeSetting = new Setting("Range", 8.0f, 3.0f, 16.0f);

    // CopyOnWriteArrayList is thread-safe for read/write from different threads
    private final CopyOnWriteArrayList<int[]> orePositions = new CopyOnWriteArrayList<>();
    private int scanTimer = 0;

    public Xray() {
        super("Xray", "Visual");
        settings.add(rangeSetting);
        alwaysShowSettings = true;
    }

    @Override
    public void onTick() {
        if (!isEnabled() || mc.player == null || mc.world == null) return;

        scanTimer++;
        if (scanTimer < 40) return; // scan every 40 ticks = 2 seconds
        scanTimer = 0;

        int range = (int) rangeSetting.value;
        List<int[]> newOres = new ArrayList<>();
        BlockPos center = mc.player.getBlockPos();
        int maxOres = 100;

        outer:
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    if (newOres.size() >= maxOres) break outer;
                    BlockPos pos = center.add(x, y, z);
                    Block block = mc.world.getBlockState(pos).getBlock();
                    int color = getOreColor(block);
                    if (color != 0) {
                        newOres.add(new int[]{pos.getX(), pos.getY(), pos.getZ(), color});
                    }
                }
            }
        }

        // Atomic swap - safe between threads
        orePositions.clear();
        orePositions.addAll(newOres);
    }

    public void renderHud(GuiGraphics ctx) {
        if (!isEnabled() || mc.player == null) return;
        for (int[] ore : orePositions) {
            Vec3 pos = new Vec3(ore[0] + 0.5, ore[1] + 0.5, ore[2] + 0.5);
            int[] screen = ArmorESP.staticWorldToScreen(pos, mc);
            if (screen == null) continue;
            int color = ore[3];
            int s = 3;
            ctx.fill(screen[0]-s, screen[1]-s, screen[0]+s, screen[1]+s, color);
        }
    }

    @Override
    public void onDisable() { orePositions.clear(); scanTimer = 0; }

    public static boolean isOreBlock(Block block) {
        return block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE
            || block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE
            || block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE
            || block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE
            || block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE
            || block == Blocks.ANCIENT_DEBRIS || block == Blocks.LAPIS_ORE
            || block == Blocks.DEEPSLATE_LAPIS_ORE || block == Blocks.COAL_ORE
            || block == Blocks.DEEPSLATE_COAL_ORE || block == Blocks.COPPER_ORE
            || block == Blocks.DEEPSLATE_COPPER_ORE || block == Blocks.NETHER_QUARTZ_ORE
            || block == Blocks.NETHER_GOLD_ORE;
    }

    private int getOreColor(Block block) {
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) return 0xFF00FFFF;
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) return 0xFF00FF44;
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE || block == Blocks.NETHER_GOLD_ORE) return 0xFFFFD700;
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) return 0xFFCCCCCC;
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) return 0xFFFF3333;
        if (block == Blocks.ANCIENT_DEBRIS) return 0xFFAA44FF;
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) return 0xFF4444FF;
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) return 0xFF555555;
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) return 0xFFFF7733;
        if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.ENDER_CHEST) return 0xFFFF8800;
        if (block == Blocks.NETHER_QUARTZ_ORE) return 0xFFFFFFFF;
        return 0;
    }
}
