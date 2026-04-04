package com.hack.modules.visual;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * InvSee — shows nearby players' visible equipment floating above them.
 *
 * Note: Can only show equipped items (armor + held items) since Minecraft
 * doesn't send full inventory contents of other players over the network.
 * Shows: helmet, chestplate, leggings, boots, main hand, offhand.
 */
public class InvSee extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    public final Setting rangeSetting = new Setting("Range", 20.0f, 5.0f, 50.0f);

    public InvSee() {
        super("InvSee", "Visual");
        settings.add(rangeSetting);
        alwaysShowSettings = true;
    }

    public void renderHud(GuiGraphics ctx) {
        if (!isEnabled() || mc.player == null || mc.level == null) return;

        for (Player player : mc.level.getPlayers()) {
            if (player == mc.player) continue;
            if (mc.player.distanceTo(player) > rangeSetting.value) continue;

            Vec3 pos = new Vec3(player.getX(),
                player.getY() + player.getHeight() + 0.8, player.getZ());
            int[] screen = ArmorESP.staticWorldToScreen(pos, mc);
            if (screen == null) continue;

            // Equipment slots to show
            ItemStack[] items = {
                player.getEquippedStack(EquipmentSlot.HEAD),
                player.getEquippedStack(EquipmentSlot.CHEST),
                player.getEquippedStack(EquipmentSlot.LEGS),
                player.getEquippedStack(EquipmentSlot.FEET),
                player.getEquippedStack(EquipmentSlot.MAINHAND),
                player.getEquippedStack(EquipmentSlot.OFFHAND),
            };

            int slotSize = 16;
            int gap = 2;
            int totalW = 6 * slotSize + 5 * gap;
            int startX = screen[0] - totalW / 2;
            int startY = screen[1] - slotSize - 2;

            // Background
            ctx.fill(startX - 2, startY - 2,
                     startX + totalW + 2, startY + slotSize + 2, 0xAA000000);

            // Draw each item
            for (int i = 0; i < items.length; i++) {
                int ix = startX + i * (slotSize + gap);
                // Slot background
                ctx.fill(ix, startY, ix + slotSize, startY + slotSize, 0x55333333);
                if (!items[i].isEmpty()) {
                    ctx.drawItem(items[i], ix, startY);
                }
            }

            // Player name above
            String name = player.getName().getString();
            int tw = mc.textRenderer.getWidth(name);
            ctx.drawTextWithShadow(mc.textRenderer, name,
                screen[0] - tw/2, startY - 10, 0xFFFFFFFF);
        }
    }
}
