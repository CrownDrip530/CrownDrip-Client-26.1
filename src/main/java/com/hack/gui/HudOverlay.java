package com.hack.gui;

import com.hack.ModuleManager;
import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Comparator;
import java.util.List;

/**
 * HudOverlay - Draws the always-visible module list and watermark.
 *
 * BUG FIXES:
 * - DrawableHelper.fill() is removed in 1.20.1. We now require a GuiGraphics
 *   to be passed in from the mixin instead of creating a bare MatrixStack.
 * - textRenderer.draw(MatrixStack, ...) signature changed in 1.20.1.
 *   We use GuiGraphics.drawText() which is the correct 1.20.1 API.
 * - render() now accepts GuiGraphics from GameRendererMixin.
 *
 * DRAWS:
 * 1. Watermark top-left: "HackClient v1.0"
 * 2. Arraylist top-right: each enabled module name in green with accent bar
 */
public class HudOverlay {

    private static final int COLOR_GREEN     = 0xFF00FF7F;
    private static final int COLOR_WATERMARK = 0xFFCCCCCC;
    private static final int COLOR_ACCENT    = 0xFF00FF7F;

    /**
     * Called every frame from GameRendererMixin.onRender().
     * GuiGraphics is the 1.20.1 way to draw text and filled rectangles.
     */
    public void render(GuiGraphics ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int screenWidth = mc.getWindow().getScaledWidth();
        int fontHeight  = mc.textRenderer.fontHeight;

        // Watermark - top left
        ctx.drawText(mc.textRenderer, "CrownDrip Client", 2, 2, COLOR_WATERMARK, true);

        // Arraylist - top right, one line per enabled module
        List<HackModule> enabled = ModuleManager.getEnabledModules();
        if (enabled.isEmpty()) return;
        enabled.sort(Comparator.comparing(HackModule::getName));

        int y = 2;
        for (HackModule module : enabled) {
            String name      = module.getName();
            int    textWidth = mc.textRenderer.getWidth(name);
            int    x         = screenWidth - textWidth - 6;

            // Green accent bar flush against right edge
            ctx.fill(screenWidth - 2, y, screenWidth, y + fontHeight, COLOR_ACCENT);

            // Module name
            ctx.drawText(mc.textRenderer, name, x, y, COLOR_GREEN, true);

            y += fontHeight + 1;
        }
    }
}
