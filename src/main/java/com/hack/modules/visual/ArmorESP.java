package com.hack.modules.visual;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

/**
 * ArmorESP — shows armor equipment and health bar above each player's head.
 *
 * Renders actual item icons (16x16) above the player's head in world space.
 * Stack order from left to right: helmet, chestplate, leggings, boots.
 * Health bar below the armor row.
 */
public class ArmorESP extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    public ArmorESP() {
        super("ArmorESP", "Visual");
    }

    public void renderHud(GuiGraphics ctx) {
        if (!isEnabled() || mc.level == null || mc.player == null) return;

        for (Player player : mc.level.getPlayers()) {
            if (player == mc.player) continue;

            // Project point 0.5 blocks above the player's head
            Vec3 pos = new Vec3(
                player.getX(),
                player.getY() + player.getHeight() + 0.5,
                player.getZ()
            );
            int[] screen = staticWorldToScreen(pos, mc);
            if (screen == null) continue;

            int cx = screen[0];
            int cy = screen[1];

            // Draw 4 armor items as 16x16 icons centered above head
            int iconSize = 16;
            int gap = 1;
            int totalW = 4 * iconSize + 3 * gap;
            int startX = cx - totalW / 2;
            int startY = cy - iconSize - 4; // 4px above the projected point

            ItemStack[] armor = {
                player.getEquippedStack(EquipmentSlot.HEAD),
                player.getEquippedStack(EquipmentSlot.CHEST),
                player.getEquippedStack(EquipmentSlot.LEGS),
                player.getEquippedStack(EquipmentSlot.FEET),
            };

            // Dark background behind all armor slots
            ctx.fill(startX - 2, startY - 2,
                     startX + totalW + 2, startY + iconSize + 2,
                     0xAA000000);

            // Draw each armor item icon
            for (int i = 0; i < 4; i++) {
                int ix = startX + i * (iconSize + gap);
                if (!armor[i].isEmpty()) {
                    ctx.drawItem(armor[i], ix, startY);
                } else {
                    // Empty slot indicator
                    ctx.fill(ix, startY, ix + iconSize, startY + iconSize, 0x33FFFFFF);
                }
            }

            // Health bar below armor
            float hp    = player.getHealth();
            float maxHp = player.getMaxHealth();
            float pct   = Math.max(0, Math.min(1, hp / maxHp));

            int barY = startY + iconSize + 3;
            int barH = 3;

            // Background
            ctx.fill(startX, barY, startX + totalW, barY + barH, 0xFF333333);

            // Fill
            int healthColor = pct > 0.5f ? 0xFF00FF44
                            : pct > 0.25f ? 0xFFFFAA00
                            : 0xFFFF3333;
            ctx.fill(startX, barY,
                     startX + (int)(totalW * pct), barY + barH,
                     healthColor);

            // HP text
            String hpText = (int)hp + "/" + (int)maxHp;
            int tw = mc.textRenderer.getWidth(hpText);
            ctx.drawTextWithShadow(mc.textRenderer, hpText,
                cx - tw / 2, barY + barH + 1, healthColor);
        }
    }

    /**
     * Projects a world position to screen coordinates.
     * Used by all visual modules.
     */
    public static int[] staticWorldToScreen(Vec3 worldPos, Minecraft mc) {
        if (mc.gameRenderer == null) return null;
        var camera = mc.gameRenderer.getCamera();
        var focusedEntity = camera.getFocusedEntity();
        if (focusedEntity == null) return null;

        double camX = focusedEntity.getX();
        double camY = focusedEntity.getY() + focusedEntity.getEyeHeight(focusedEntity.getPose());
        double camZ = focusedEntity.getZ();

        double dx = worldPos.x - camX;
        double dy = worldPos.y - camY;
        double dz = worldPos.z - camZ;

        float yaw   = (float) Math.toRadians(camera.getYaw());
        float pitch = (float) Math.toRadians(camera.getPitch());

        double sinYaw = Math.sin(yaw), cosYaw = Math.cos(yaw);
        double sinPit = Math.sin(pitch), cosPit = Math.cos(pitch);

        // Negate cx to fix left-right mirror in Minecraft's coordinate system
        double cx = -(dx * cosYaw - dz * sinYaw);
        double cy =   dy * cosPit + (dx * sinYaw + dz * cosYaw) * sinPit;
        double cz =  -dy * sinPit + (dx * sinYaw + dz * cosYaw) * cosPit;

        if (cz <= 0) return null;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        double fov   = Math.toRadians((double)(int) mc.options.getFov().getValue());
        double scale = (sh / 2.0) / Math.tan(fov / 2.0);

        int screenX = (int)(sw / 2.0 + cx / cz * scale);
        int screenY = (int)(sh / 2.0 - cy / cz * scale);

        if (screenX < -200 || screenX > sw + 200
         || screenY < -200 || screenY > sh + 200) return null;

        return new int[]{screenX, screenY};
    }

    private int[] worldToScreen(Vec3 worldPos) {
        return staticWorldToScreen(worldPos, mc);
    }
}
