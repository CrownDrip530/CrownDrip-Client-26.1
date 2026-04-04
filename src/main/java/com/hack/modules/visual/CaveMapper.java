package com.hack.modules.visual;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;

import java.util.concurrent.atomic.AtomicReference;

/**
 * CaveMapper — 2D minimap showing caves at player Y level.
 * Uses AtomicReference for thread-safe map data swap.
 */
public class CaveMapper extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    public final Setting rangeSetting = new Setting("Range", 24.0f, 8.0f, 48.0f);
    public final Setting sizeSetting  = new Setting("Size",  80.0f, 40.0f, 160.0f);

    private final AtomicReference<int[][]> mapRef = new AtomicReference<>(new int[0][0]);
    private int scanTimer = 0;

    public CaveMapper() {
        super("CaveMapper", "Visual");
        settings.add(rangeSetting);
        settings.add(sizeSetting);
    }

    @Override
    public void onTick() {
        if (!isEnabled() || mc.player == null || mc.world == null) return;

        scanTimer++;
        if (scanTimer < 20) return; // scan every 20 ticks = 1 second
        scanTimer = 0;

        int range = (int) rangeSetting.value;
        int diameter = range * 2 + 1;
        int[][] newMap = new int[diameter][diameter];

        BlockPos center = mc.player.getBlockPos();
        int playerY = center.getY();

        for (int x = 0; x < diameter; x++) {
            for (int z = 0; z < diameter; z++) {
                BlockPos pos = new BlockPos(
                    center.getX() - range + x,
                    playerY,
                    center.getZ() - range + z
                );
                if (Xray.isOreBlock(mc.world.getBlockState(pos).getBlock()))
                    newMap[x][z] = 0xFFFFFF00;
                else if (mc.world.getBlockState(pos).isAir())
                    newMap[x][z] = 0xFF88AAFF;
                else
                    newMap[x][z] = 0xFF222222;
            }
        }

        mapRef.set(newMap); // atomic swap
    }

    public void renderHud(GuiGraphics ctx) {
        if (!isEnabled() || mc.player == null) return;
        int[][] mapData = mapRef.get();
        if (mapData.length == 0) return;

        int mapSize = (int) sizeSetting.value;
        int sw = mc.getWindow().getScaledWidth();
        int startX = sw - mapSize - 4;
        int startY = 4;

        ctx.fill(startX - 1, startY - 1, startX + mapSize + 1, startY + mapSize + 1, 0xAA000000);

        int cells = mapData.length;
        float cellSize = (float) mapSize / cells;

        for (int x = 0; x < cells; x++) {
            for (int z = 0; z < cells; z++) {
                int color = mapData[x][z];
                int px = startX + (int)(x * cellSize);
                int py = startY + (int)(z * cellSize);
                int pw = Math.max(1, (int) cellSize);
                ctx.fill(px, py, px + pw, py + pw, color);
            }
        }

        // Player dot
        ctx.fill(startX + mapSize/2 - 1, startY + mapSize/2 - 1,
                 startX + mapSize/2 + 2, startY + mapSize/2 + 2, 0xFFFF4444);

        ctx.drawTextWithShadow(mc.textRenderer, "Caves",
                startX, startY + mapSize + 2, 0xFF888888);
    }

    @Override
    public void onDisable() { mapRef.set(new int[0][0]); scanTimer = 0; }
}
