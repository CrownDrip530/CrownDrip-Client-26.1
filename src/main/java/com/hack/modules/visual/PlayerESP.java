package com.hack.modules.visual;

import com.hack.modules.visual.ArmorESP;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * PlayerESP - Draws 2D boxes around players on screen.
 *
 * 1.21.11: The 3D world rendering API changed completely.
 * We use screen-space rendering via InGameHudMixin instead.
 * worldToScreen() projects player bounding box corners to screen coords.
 */
public class PlayerESP extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    public PlayerESP() {
        super("PlayerESP", "Visual");
    }

    public void renderHud(GuiGraphics ctx) {
        if (!isEnabled() || mc.level == null || mc.player == null) return;

        for (Player player : mc.level.getPlayers()) {
            if (player == mc.player) continue;

            Box bb = player.getBoundingBox();

            // Project top and bottom of bounding box to screen
            Vec3 top    = new Vec3(player.getX(), bb.maxY + 0.1, player.getZ());
            Vec3 bottom = new Vec3(player.getX(), bb.minY - 0.1, player.getZ());
            Vec3 left   = new Vec3(bb.minX, player.getY() + player.getHeight() / 2, player.getZ());
            Vec3 right  = new Vec3(bb.maxX, player.getY() + player.getHeight() / 2, player.getZ());

            int[] st = worldToScreen(top);
            int[] sb = worldToScreen(bottom);
            int[] sl = worldToScreen(left);
            int[] sr = worldToScreen(right);

            if (st == null || sb == null || sl == null || sr == null) continue;

            int x1 = Math.min(sl[0], sr[0]);
            int x2 = Math.max(sl[0], sr[0]);
            int y1 = st[1];
            int y2 = sb[1];

            if (y1 > y2) { int tmp = y1; y1 = y2; y2 = tmp; }

            // Red outline box
            int color = 0xFFFF3333;
            ctx.fill(x1,   y1,   x2,   y1+1, color); // top
            ctx.fill(x1,   y2-1, x2,   y2,   color); // bottom
            ctx.fill(x1,   y1,   x1+1, y2,   color); // left
            ctx.fill(x2-1, y1,   x2,   y2,   color); // right
        }
    }

    public int[] worldToScreen(Vec3 worldPos) {
        return ArmorESP.staticWorldToScreen(worldPos, mc);
    }
}
