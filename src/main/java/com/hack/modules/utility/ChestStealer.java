package com.hack.modules.utility;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;

/**
 * ChestStealer — automatically takes all items from open chests.
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

        for (int i = 0; i < chestSlots; i++) {
            if (!handler.getSlot(i).getItem().isEmpty()) {
                mc.gameMode.handleInventoryMouseClick(syncId, i, 0, ClickType.QUICK_MOVE, p);
                delay = 2;
                return;
            }
        }
    }
}
