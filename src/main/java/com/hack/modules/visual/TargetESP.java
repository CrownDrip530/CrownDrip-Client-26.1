package com.hack.modules.visual;

import com.hack.HackClient;
import com.hack.modules.HackModule;
import com.hack.modules.combat.KillAura;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * TargetESP — draws a special highlighted box around KillAura's current target.
 *
 * Shows: animated pulsing box in bright red/orange, health bar,
 * distance, and name. Much more prominent than regular PlayerESP.
 */
public class TargetESP extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    private int animTick = 0;

    public TargetESP() {
        super("TargetESP", "Visual");
    }

    public void renderHud(GuiGraphics ctx) {
        if (!isEnabled() || mc.player == null) return;
        if (HackClient.moduleManager == null) return;

        KillAura ka = HackClient.moduleManager.get(KillAura.class);
        if (ka == null || !ka.isEnabled()) return;

        LivingEntity target = ka.getCurrentTarget();
        if (target == null) return;

        animTick++;

        Box bb = target.getBoundingBox();
        Vec3 top    = new Vec3(target.getX(), bb.maxY + 0.2, target.getZ());
        Vec3 bottom = new Vec3(target.getX(), bb.minY - 0.2, target.getZ());
        Vec3 left   = new Vec3(bb.minX - 0.1, target.getY() + target.getHeight()/2, target.getZ());
        Vec3 right  = new Vec3(bb.maxX + 0.1, target.getY() + target.getHeight()/2, target.getZ());

        int[] st = ArmorESP.staticWorldToScreen(top, mc);
        int[] sb = ArmorESP.staticWorldToScreen(bottom, mc);
        int[] sl = ArmorESP.staticWorldToScreen(left, mc);
        int[] sr = ArmorESP.staticWorldToScreen(right, mc);

        if (st == null || sb == null || sl == null || sr == null) return;

        int x1 = Math.min(sl[0], sr[0]);
        int x2 = Math.max(sl[0], sr[0]);
        int y1 = st[1];
        int y2 = sb[1];
        if (y1 > y2) { int tmp = y1; y1 = y2; y2 = tmp; }

        // Pulsing color - alternates between bright red and orange
        float pulse = (float)(Math.sin(animTick * 0.2) + 1) / 2;
        int r = 255;
        int g = (int)(pulse * 165);
        int color = 0xFF000000 | (r << 16) | (g << 8);

        // Thick box (2px)
        ctx.fill(x1,   y1,   x2,   y1+2, color);
        ctx.fill(x1,   y2-2, x2,   y2,   color);
        ctx.fill(x1,   y1,   x1+2, y2,   color);
        ctx.fill(x2-2, y1,   x2,   y2,   color);

        // Corner accents
        int cs = 6;
        ctx.fill(x1, y1, x1+cs, y1+2, 0xFFFFFFFF);
        ctx.fill(x1, y1, x1+2, y1+cs, 0xFFFFFFFF);
        ctx.fill(x2-cs, y1, x2, y1+2, 0xFFFFFFFF);
        ctx.fill(x2-2, y1, x2, y1+cs, 0xFFFFFFFF);
        ctx.fill(x1, y2-2, x1+cs, y2, 0xFFFFFFFF);
        ctx.fill(x1, y2-cs, x1+2, y2, 0xFFFFFFFF);
        ctx.fill(x2-cs, y2-2, x2, y2, 0xFFFFFFFF);
        ctx.fill(x2-2, y2-cs, x2, y2, 0xFFFFFFFF);

        // Name + distance + health above box
        String name = target.getName().getString();
        float hp = target.getHealth();
        float maxHp = target.getMaxHealth();
        int dist = (int) mc.player.distanceTo(target);
        int hpColor = hp/maxHp > 0.5f ? 0x00FF44 : hp/maxHp > 0.25f ? 0xFFAA00 : 0xFF3333;
        net.minecraft.network.chat.Text info = net.minecraft.network.chat.Component.literal(name + " ")
            .append(net.minecraft.network.chat.Component.literal((int)hp + "/" + (int)maxHp)
                .styled(s -> s.withColor(hpColor)))
            .append(net.minecraft.network.chat.Component.literal(" [" + dist + "m]")
                .styled(s -> s.withColor(0xAAAAAA)));
        int tw = mc.textRenderer.getWidth(info);
        ctx.fill((x1+x2)/2 - tw/2 - 2, y1 - 12,
                 (x1+x2)/2 + tw/2 + 2, y1 - 1, 0xAA000000);
        ctx.drawText(mc.textRenderer, info,
                (x1+x2)/2 - tw/2, y1 - 11, 0xFFFFFF, true);

        // Health bar below box
        int barW = x2 - x1;
        float pct = hp / maxHp;
        int healthColor = pct > 0.5f ? 0xFF00FF44 : pct > 0.25f ? 0xFFFFAA00 : 0xFFFF3333;
        ctx.fill(x1, y2+2, x2, y2+5, 0xFF333333);
        ctx.fill(x1, y2+2, x1+(int)(barW*pct), y2+5, healthColor);
    }

    @Override
    public void onDisable() { animTick = 0; }
}
