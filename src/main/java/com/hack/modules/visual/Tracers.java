package com.hack.modules.visual;

import com.hack.modules.visual.ArmorESP;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Tracers - Draws lines from screen center to each player.
 * Uses screen-space rendering via InGameHudMixin.
 */
public class Tracers extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    public Tracers() {
        super("Tracers", "Visual");
    }

    public void renderHud(GuiGraphics ctx) {
        if (!isEnabled() || mc.world == null || mc.player == null) return;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int centerX = sw / 2;
        int centerY = sh / 2;

        for (Player player : mc.world.getPlayers()) {
            if (player == mc.player) continue;

            Vec3 pos = new Vec3(player.getX(), player.getY() + player.getHeight() / 2, player.getZ());
            int[] screen = ArmorESP.staticWorldToScreen(pos, mc);
            if (screen == null) continue;

            // Draw line from crosshair to player using fill rectangles
            drawLine(ctx, centerX, centerY, screen[0], screen[1], 0xFFFFFF00);
        }
    }

    private void drawLine(GuiGraphics ctx, int x1, int y1, int x2, int y2, int color) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int steps = Math.max(dx, dy);
        if (steps == 0) return;
        steps = Math.min(steps, 500); // cap at 500 pixels

        for (int i = 0; i <= steps; i++) {
            int x = x1 + (x2 - x1) * i / steps;
            int y = y1 + (y2 - y1) * i / steps;
            ctx.fill(x, y, x + 1, y + 1, color);
        }
    }
}
