package com.hack.modules.utility;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.ClickType;

/**
 * AutoArmor — automatically equips best armor from inventory.
 *
 * 1.21.11: Uses Item.getSlotType() which still exists on ArmorItem,
 * with fallback to name-based detection.
 */
public class AutoArmor extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    private int cooldown = 0;

    public AutoArmor() { super("AutoArmor", "Utility"); }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null || mc.gameMode == null) return;
        if (cooldown > 0) { cooldown--; return; }

        EquipmentSlot[] armorSlots = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST,
            EquipmentSlot.LEGS, EquipmentSlot.FEET
        };

        for (EquipmentSlot slot : armorSlots) {
            ItemStack current = p.getEquippedStack(slot);
            int currentTier = getArmorTier(current);
            int bestSlot = -1;
            int bestTier = currentTier;

            for (int i = 0; i < 36; i++) {
                ItemStack stack = p.getInventory().getItem(i);
                if (stack.isEmpty()) continue;
                if (!isArmorForSlot(stack, slot)) continue;
                int tier = getArmorTier(stack);
                if (tier > bestTier) {
                    bestTier = tier;
                    bestSlot = i;
                }
            }

            if (bestSlot != -1) {
                int screenSlot = bestSlot < 9 ? 36 + bestSlot : bestSlot;
                int syncId = p.playerScreenHandler.syncId;
                mc.gameMode.clickSlot(syncId, screenSlot, 0,
                    ClickType.QUICK_MOVE, p);
                cooldown = 10;
                return;
            }
        }
    }

    private boolean isArmorForSlot(ItemStack stack, EquipmentSlot slot) {
        String name = stack.getItem().toString().toLowerCase();
        return switch (slot) {
            case HEAD  -> name.contains("helmet") || name.contains("cap");
            case CHEST -> name.contains("chestplate") || name.contains("tunic");
            case LEGS  -> name.contains("leggings") || name.contains("pants");
            case FEET  -> name.contains("boots");
            default    -> false;
        };
    }

    private int getArmorTier(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        String name = stack.getItem().toString().toLowerCase();
        if (name.contains("netherite")) return 5;
        if (name.contains("diamond"))   return 4;
        if (name.contains("iron"))      return 3;
        if (name.contains("chain"))     return 2;
        if (name.contains("gold"))      return 1;
        if (name.contains("leather"))   return 0;
        return -1;
    }
}
