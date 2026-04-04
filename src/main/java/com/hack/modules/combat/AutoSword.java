package com.hack.modules.combat;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * AutoSword — automatically switches to best sword/weapon before attacking.
 */
public class AutoSword extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    private int savedSlot = -1;

    public AutoSword() { super("AutoSword", "Combat"); }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;

        if (!mc.options.attackKey.isPressed()) {
            restoreSlot(p); return;
        }

        int bestSlot = -1;
        int bestTier = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = p.getInventory().getStack(i);
            int tier = getWeaponTier(stack);
            if (tier > bestTier) { bestTier = tier; bestSlot = i; }
        }

        if (bestSlot != -1 && bestSlot != p.getInventory().getSelectedSlot()) {
            if (savedSlot == -1) savedSlot = p.getInventory().getSelectedSlot();
            p.getInventory().setSelectedSlot(bestSlot);
            p.networkHandler.sendPacket(p.getInventory().createSlotSetPacket(bestSlot));
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) restoreSlot(mc.player);
    }

    private int getWeaponTier(ItemStack stack) {
        if (stack.isEmpty()) return -1;
        String name = stack.getItem().toString().toLowerCase();
        int material = 0;
        if (name.contains("netherite")) material = 50;
        else if (name.contains("diamond")) material = 40;
        else if (name.contains("iron")) material = 30;
        else if (name.contains("stone")) material = 20;
        else if (name.contains("gold")) material = 15;
        else if (name.contains("wood")) material = 10;

        if (name.contains("sword")) return material + 2;
        if (name.contains("axe")) return material + 1;
        return material > 0 ? material : -1;
    }

    private void restoreSlot(LocalPlayer p) {
        if (savedSlot != -1) {
            p.getInventory().setSelectedSlot(savedSlot);
            p.networkHandler.sendPacket(p.getInventory().createSlotSetPacket(savedSlot));
            savedSlot = -1;
        }
    }
}
