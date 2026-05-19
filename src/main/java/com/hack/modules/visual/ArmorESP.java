package com.hack.modules.visual;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * ArmorESP - Renders armor icons and health bar above players.
 */
public class ArmorESP extends HackModule {
    private final Minecraft mc = Minecraft.getInstance();

    public ArmorESP() {
        super("ArmorESP", "Visual");
    }

    public void renderHud(GuiGraphics ctx) {
        if (!isEnabled() || mc.level == null || mc.player == null) return;

        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;

            Vec3 pos = new Vec3(
                player.getX(),
                player.getY() + player.getBbHeight() + 0.5,
                player.getZ()
            );

            int[] screen = staticWorldToScreen(pos, mc);
            if (screen == null) continue;

            int cx = screen[0];
            int cy = screen[1];

            int iconSize = 16;
            int gap = 1;
            int totalW = 4 * iconSize + 3 * gap;
            int startX = cx - totalW / 2;
            int startY = cy - iconSize - 4;

            ItemStack[] armor = {
                player.getItemBySlot(EquipmentSlot.HEAD),
                player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.FEET),
            };

            ctx.fill(startX - 2, startY - 2,
                     startX + totalW + 2, startY + iconSize + 2,
                     0xAA000000);

            for (int i = 0; i < 4; i++) {
                int ix = startX + i * (iconSize + gap);
                if (!armor[i].isEmpty()) {
                    ctx.renderItem(armor[i], ix, startY);
                } else {
                    ctx.fill(ix, startY, ix + iconSize, startY + iconSize, 0x33FFFFFF);
                }
            }

            float hp    = player.getHealth();
            float maxHp = player.getMaxHealth();
            float pct   = Math.max(0, Math.min(1, hp / maxHp));
            int barY = startY + iconSize + 3;
            int barH = 3;

            ctx.fill(startX, barY, startX + totalW, barY + barH, 0xFF333333);

            int healthColor = pct > 0.5f ? 0xFF00FF44
                            : pct > 0.25f ? 0xFFFFAA00
                            : 0xFFFF3333;
            ctx.fill(startX, barY,
                     startX + (int)(totalW * pct), barY + barH,
                     healthColor);

            String hpText = (int)hp + "/" + (int)maxHp;
            int tw = mc.font.width(hpText);
            ctx.drawString(mc.font, hpText, cx - tw / 2, barY + barH + 1, healthColor, true);
        }
    }

    public static int[] staticWorldToScreen(Vec3 worldPos, Minecraft mc) {
        if (mc.gameRenderer == null) return null;
        var camera = mc.gameRenderer.getMainCamera();
        var focusedEntity = camera.getEntity();
        if (focusedEntity == null) return null;

        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;

        double dx = worldPos.x - camX;
        double dy = worldPos.y - camY;
        double dz = worldPos.z - camZ;

        float yaw   = (float) Math.toRadians(camera.getYRot());
        float pitch = (float) Math.toRadians(camera.getXRot());

        double sinYaw = Math.sin(yaw), cosYaw = Math.cos(yaw);
        double sinPit = Math.sin(pitch), cosPit = Math.cos(pitch);

        double cx = -(dx * cosYaw - dz * sinYaw);
        double cy =   dy * cosPit + (dx * sinYaw + dz * cosYaw) * sinPit;
        double cz =  -dy * sinPit + (dx * sinYaw + dz * cosYaw) * cosPit;

        if (cz <= 0) return null;

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        double fov   = Math.toRadians((double) mc.options.fov().get());
        double scale = (sh / 2.0) / Math.tan(fov / 2.0);

        int screenX = (int)(sw / 2.0 + cx / cz * scale);
        int screenY = (int)(sh / 2.0 - cy / cz * scale);

        if (screenX < -200 || screenX > sw + 200
         || screenY < -200 || screenY > sh + 200) return null;

        return new int[]{screenX, screenY};
    }
}
