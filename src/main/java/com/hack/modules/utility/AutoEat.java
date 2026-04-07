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

        if (p.getFoodData().getFoodLevel() >= (int) threshSetting.value) {
            restoreSlot(p); return;
        }

        // Find food in hotbar
        int foodSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = p.getInventory().getItem(i);
            // Food items have a use time > 0 (time to eat/drink)
            if (!stack.isEmpty() && stack.getUseDuration(p) > 0) {
                foodSlot = i; break;
            }
        }
        if (foodSlot == -1) { restoreSlot(p); return; }

        if (savedSlot == -1) savedSlot = p.getInventory().getSelectedSlot();
        p.getInventory().setSelectedSlot(foodSlot);
        mc.getConnection().sendPacket(new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(foodSlot));

        // Hold use key to eat
        mc.options.keyUse.setDown(true);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.options.keyUse.setDown(false);
            restoreSlot(mc.player);
        }
    }

    private void restoreSlot(LocalPlayer p) {
        mc.options.keyUse.setDown(false);
        if (savedSlot != -1) {
            p.getInventory().setSelectedSlot(savedSlot);
            mc.getConnection().sendPacket(new net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket(savedSlot));
            savedSlot = -1;
        }
    }
}
