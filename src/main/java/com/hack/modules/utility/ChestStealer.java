package com.hack.modules.utility;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;

/**
 * ChestStealer — automatically takes all items from open chests.
 * Just open a chest and it empties it into your inventory.
 */
public class ChestStealer extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    private int delay = 0;

    public ChestStealer() { super("ChestStealer", "Utility"); }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null || mc.gameMode == null) return;
        if (delay > 0) { delay--; return; }

        if (!(p.containerMenu instanceof ChestMenu handler))
            return;

        int syncId = handler.containerId;
        int chestSlots = handler.getContainer().getContainerSize();

        // Shift-click each chest slot to take items
        for (int i = 0; i < chestSlots; i++) {
            if (!handler.getSlot(i).getItem().isEmpty()) {
                mc.gameMode.clickSlot(syncId, i, 0,
                    ClickType.QUICK_MOVE, p);
                delay = 2;
                return; // one per 2 ticks
            }
        }
    }
}
