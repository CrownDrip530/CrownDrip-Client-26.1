package com.hack.modules.utility;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * AutoEat — automatically eats food when hunger drops below threshold.
 */
public class AutoEat extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    public final Setting threshSetting = new Setting("Hunger", 15.0f, 1.0f, 20.0f);
    private int savedSlot = -1;

    public AutoEat() {
        super("AutoEat", "Utility");
        settings.add(threshSetting);
        alwaysShowSettings = true;
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null || mc.gameMode == null) return;

        if (p.getHungerManager().getFoodLevel() >= (int) threshSetting.value) {
            restoreSlot(p); return;
        }

        // Find food in hotbar
        int foodSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = p.getInventory().getStack(i);
            // Food items have a use time > 0 (time to eat/drink)
            if (!stack.isEmpty() && stack.getMaxUseTime(p) > 0) {
                foodSlot = i; break;
            }
        }
        if (foodSlot == -1) { restoreSlot(p); return; }

        if (savedSlot == -1) savedSlot = p.getInventory().getSelectedSlot();
        p.getInventory().setSelectedSlot(foodSlot);
        p.networkHandler.sendPacket(p.getInventory().createSlotSetPacket(foodSlot));

        // Hold use key to eat
        mc.options.useKey.setPressed(true);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.options.useKey.setPressed(false);
            restoreSlot(mc.player);
        }
    }

    private void restoreSlot(LocalPlayer p) {
        mc.options.useKey.setPressed(false);
        if (savedSlot != -1) {
            p.getInventory().setSelectedSlot(savedSlot);
            p.networkHandler.sendPacket(p.getInventory().createSlotSetPacket(savedSlot));
            savedSlot = -1;
        }
    }
}
