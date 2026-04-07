package com.hack.modules.utility;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * AutoTotem — automatically equips a totem of undying to the off-hand.
 *
 * FIXES:
 * - Use correct syncId: playerScreenHandler.syncId is always valid (it's
 *   the player inventory handler, always open). Confirmed correct for 1.20.1.
 * - Added check that interactionManager is not null before clicking slots.
 * - Off-hand slot is 45 in the player inventory screen — confirmed correct.
 * - Added cooldown (5 ticks) so we don't spam slot clicks every tick.
 * - Swap logic is now: pickup totem → place in offhand → if cursor not empty,
 *   put back in original slot. This handles the case where offhand had an item.
 *
 * MODES:
 * - Always = 0: only equip when health <= threshold
 * - Always = 1: always keep totem in off-hand
 */
public class AutoTotem extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    public final Setting healthSetting = new Setting("HP Threshold", 10.0f, 1.0f, 20.0f);
    public final Setting alwaysSetting = new Setting("Always",        0.0f, 0.0f,  1.0f);

    private int cooldown = 0; // ticks since last swap (avoid spam)

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

        // Cooldown to avoid spamming slot clicks
        if (cooldown > 0) { cooldown--; return; }

        // Already has totem in off-hand — nothing to do
        if (p.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;

        // Decide whether to act
        boolean shouldSwap = alwaysSetting.value >= 0.5f
                || p.getHealth() <= healthSetting.value;
        if (!shouldSwap) return;

        // Find totem in inventory
        int totemSlot = -1;
        // Only search hotbar (0-8) and main inventory (9-35), not armor/offhand
        for (int i = 0; i < 36; i++) {
            if (p.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlot = i;
                break;
            }
        }
        if (totemSlot == -1) return; // no totem found

        // Convert inventory slot → screen slot
        // Hotbar slots 0-8 → screen slots 36-44
        // Main inventory 9-35 → screen slots 9-35
        int screenSlot = totemSlot < 9 ? 36 + totemSlot : totemSlot;

        int syncId = p.playerScreenHandler.syncId;

        // Step 1: Pick up the totem from its slot
        mc.gameMode.clickSlot(syncId, screenSlot, 0,
                ClickType.PICKUP, p);

        // Step 2: Place it in the off-hand slot
        mc.gameMode.clickSlot(syncId, InventoryMenu.OFFHAND_ID, 0,
                ClickType.PICKUP, p);

        // Step 3: If there was something in the off-hand, put it back
        if (!p.playerScreenHandler.getCursorStack().isEmpty()) {
            mc.gameMode.clickSlot(syncId, screenSlot, 0,
                    ClickType.PICKUP, p);
        }

        cooldown = 5; // wait 5 ticks before next swap attempt
    }
}
