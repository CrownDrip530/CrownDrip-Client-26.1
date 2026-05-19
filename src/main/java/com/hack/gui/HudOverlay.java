package com.hack.gui;

import com.hack.ModuleManager;
import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import java.util.Comparator;
import java.util.List;

/**
 * HudOverlay - Draws the always-visible module list and watermark.
 */
public class HudOverlay {
    private static final int COLOR_GREEN     = 0xFF00FF7F;
    private static final int COLOR_WATERMARK = 0xFFCCCCCC;
    private static final int COLOR_ACCENT    = 0xFF00FF7F;

    public void render(GuiGraphics ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int fontHeight  = mc.font.lineHeight;

        ctx.drawString(mc.font, "CrownDrip Client", 2, 2, COLOR_WATERMARK, true);

        List<HackModule> enabled = ModuleManager.getEnabledModules();
        if (enabled.isEmpty()) return;
        enabled.sort(Comparator.comparing(HackModule::getName));

        int y = 2;
        for (HackModule module : enabled) {
            String name      = module.getName();
            int    textWidth = mc.font.width(name);
            int    x         = screenWidth - textWidth - 6;

            ctx.fill(screenWidth - 2, y, screenWidth, y + fontHeight, COLOR_ACCENT);
            ctx.drawString(mc.font, name, x, y, COLOR_GREEN, true);
            y += fontHeight + 1;
        }
    }
}
