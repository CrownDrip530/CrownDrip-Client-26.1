package com.hack.modules.combat;

import com.hack.modules.HackModule;
import com.hack.modules.visual.ArmorESP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * HitboxExtend - Expands enemy hitboxes making them easier to hit.
 *
 * FIXES:
 * - Only expands OTHER players' hitboxes (HitboxMixin excludes local player)
 * - Renders expanded hitbox outlines on screen so you can see them
 * - No performance impact on local player movement/collision
 */
public class HitboxExtend extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    public final Setting expandSetting = new Setting("Size", 0.5f, 0.0f, 5.0f);

    public HitboxExtend() {
        super("HitboxExtend", "Combat");
        settings.add(expandSetting);
        alwaysShowSettings = true;
    }

    public void renderHud(GuiGraphics ctx) {
        if (!isEnabled() || mc.level == null || mc.player == null) return;
        if (expandSetting.value <= 0) return;

        for (Player player : mc.level.getPlayers()) {
            if (player == mc.player) continue;

            float exp = expandSetting.value;
            // Calculate expanded box from player dimensions directly
            // (don't call getBoundingBox() - it already returns expanded box via HitboxMixin)
            double hw = player.getWidth() / 2.0 + exp; // half-width + expansion
            double ht = player.getHeight() + exp;       // height + expansion
            Box bb = new Box(
                player.getX() - hw, player.getY() - exp, player.getZ() - hw,
                player.getX() + hw, player.getY() + ht,  player.getZ() + hw
            );

            // Project expanded box corners to screen
            Vec3 top    = new Vec3(player.getX(), bb.maxY, player.getZ());
            Vec3 bottom = new Vec3(player.getX(), bb.minY, player.getZ());
            Vec3 left   = new Vec3(bb.minX, player.getY() + player.getHeight()/2, player.getZ());
            Vec3 right  = new Vec3(bb.maxX, player.getY() + player.getHeight()/2, player.getZ());

            int[] st = ArmorESP.staticWorldToScreen(top, mc);
            int[] sb = ArmorESP.staticWorldToScreen(bottom, mc);
            int[] sl = ArmorESP.staticWorldToScreen(left, mc);
            int[] sr = ArmorESP.staticWorldToScreen(right, mc);

            if (st == null || sb == null || sl == null || sr == null) continue;

            int x1 = Math.min(sl[0], sr[0]);
            int x2 = Math.max(sl[0], sr[0]);
            int y1 = st[1];
            int y2 = sb[1];

            if (y1 > y2) { int tmp = y1; y1 = y2; y2 = tmp; }

            // Cyan dashed box outline
            int color = 0xFF00FFFF;
            ctx.fill(x1,   y1,   x2,   y1+1, color);
            ctx.fill(x1,   y2-1, x2,   y2,   color);
            ctx.fill(x1,   y1,   x1+1, y2,   color);
            ctx.fill(x2-1, y1,   x2,   y2,   color);
        }
    }
}
