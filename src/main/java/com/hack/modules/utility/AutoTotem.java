package com.hack.modules.utility;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * AutoTotem — automatically equips a totem of undying to the off-hand.
 */
public class AutoTotem extends HackModule {
    private final Minecraft mc = Minecraft.getInstance();
    public final Setting healthSetting = new Setting("HP Threshold", 10.0f, 1.0f, 20.0f);
    public final Setting alwaysSetting = new Setting("Always",        0.0f, 0.0f,  1.0f);
    private int cooldown = 0;

    public AutoTotem() {
        super("AutoTotem", "Utility");
        settings.add(healthSetting);
        settings.add(alwaysSetting);
    }

    @Override
    public void onDisable() { cooldown = 0; }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        if (mc.gameMode == null) return;
        LocalPlayer p = mc.player;
        if (p == null) return;

        if (cooldown > 0) { cooldown--; return; }
        if (p.getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING) return;

        boolean shouldSwap = alwaysSetting.value >= 0.5f
                || p.getHealth() <= healthSetting.value;
        if (!shouldSwap) return;

        int totemSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (p.getInventory().getItem(i).getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlot = i;
                break;
            }
        }
        if (totemSlot == -1) return;

        int screenSlot = totemSlot < 9 ? 36 + totemSlot : totemSlot;
        int syncId = p.inventoryMenu.containerId;

        mc.gameMode.handleInventoryMouseClick(syncId, screenSlot, 0, ClickType.PICKUP, p);
        mc.gameMode.handleInventoryMouseClick(syncId, InventoryMenu.SHIELD_SLOT, 0, ClickType.PICKUP, p);

        if (!p.inventoryMenu.getCarried().isEmpty()) {
            mc.gameMode.handleInventoryMouseClick(syncId, screenSlot, 0, ClickType.PICKUP, p);
        }

        cooldown = 5;
    }
}
