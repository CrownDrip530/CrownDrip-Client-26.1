package com.hack.modules.visual;

import com.hack.modules.HackModule;
import net.minecraft.world.level.block.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * StorageESP — highlights chests and containers through walls.
 * Thread-safe with CopyOnWriteArrayList.
 */
public class StorageESP extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    public final Setting rangeSetting = new Setting("Range", 16.0f, 5.0f, 32.0f);

    private final CopyOnWriteArrayList<int[]> storagePositions = new CopyOnWriteArrayList<>();
    private int scanTimer = 0;

    public StorageESP() {
        super("StorageESP", "Visual");
        settings.add(rangeSetting);
        alwaysShowSettings = true;
    }

    @Override
    public void onTick() {
        if (!isEnabled() || mc.player == null || mc.level == null) return;
        scanTimer++;
        if (scanTimer < 40) return;
        scanTimer = 0;

        int range = (int) rangeSetting.value;
        List<int[]> found = new ArrayList<>();
        BlockPos center = mc.player.getBlockPos();

        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    if (found.size() >= 100) break;
                    BlockPos pos = center.offset(x, y, z);
                    Block block = mc.level.getBlockState(pos).getBlock();
                    int color = getStorageColor(block);
                    if (color != 0) found.add(new int[]{pos.getX(), pos.getY(), pos.getZ(), color});
                }
            }
        }

        storagePositions.clear();
        storagePositions.addAll(found);
    }

    public void renderHud(GuiGraphics ctx) {
        if (!isEnabled() || mc.player == null) return;
        for (int[] s : storagePositions) {
            Vec3 pos = new Vec3(s[0] + 0.5, s[1] + 0.5, s[2] + 0.5);
            int[] screen = ArmorESP.staticWorldToScreen(pos, mc);
            if (screen == null) continue;
            ctx.fill(screen[0]-4, screen[1]-4, screen[0]+4, screen[1]+4, s[3]);
            ctx.fill(screen[0]-3, screen[1]-3, screen[0]+3, screen[1]+3, 0x88000000);
        }
    }

    @Override
    public void onDisable() { storagePositions.clear(); scanTimer = 0; }

    private int getStorageColor(Block block) {
        if (block instanceof ChestBlock || block instanceof TrappedChestBlock) return 0xFFFF8800;
        if (block instanceof EnderChestBlock) return 0xFF8800FF;
        if (block instanceof FurnaceBlock || block instanceof BlastFurnaceBlock
            || block instanceof SmokerBlock) return 0xFFFF4400;
        if (block instanceof BarrelBlock) return 0xFF885500;
        if (block instanceof ShulkerBoxBlock) return 0xFFFF00FF;
        if (block instanceof HopperBlock) return 0xFF888888;
        if (block instanceof DispenserBlock || block instanceof DropperBlock) return 0xFF888888;
        return 0;
    }
}
