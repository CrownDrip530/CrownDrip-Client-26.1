package com.hack.gui.components;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Slider - A draggable slider widget for adjusting module settings.
 *
 * Used in ModulePanel when a module is expanded (right-clicked).
 * Renders as:
 *   Label: "Reach: 6.0"
 *   [====|----------]   <- gray bar, blue fill, white handle
 *
 * How it works:
 * - draw() renders the bar, fill, handle, and label every frame
 * - onMouseClick() checks if a click lands on the slider and starts dragging
 * - onMouseDrag() updates the value while dragging
 * - onMouseRelease() stops dragging
 *
 * The slider modifies setting.value directly.
 * The module reads setting.value in its onTick() method.
 * So dragging the slider immediately affects the hack behavior.
 */
public class Slider {

    private final HackModule.Setting setting;
    private boolean dragging = false;

    // Colors (ARGB format: 0xAARRGGBB)
    private static final int COLOR_BG     = 0xFF333333;  // dark gray background bar
    private static final int COLOR_FILL   = 0xFF0077FF;  // blue filled portion
    private static final int COLOR_HANDLE = 0xFFFFFFFF;  // white draggable handle
    private static final int COLOR_TEXT   = 0xFFAAAAAA;  // muted gray label text

    public Slider(HackModule.Setting setting) {
        this.setting = setting;
    }

    /**
     * Draws the slider at the given position.
     *
     * @param ctx    GuiGraphics from the GUI render method
     * @param x      left edge of the slider bar
     * @param y      top edge of the slider bar
     * @param width  total width of the slider bar
     */
    public void draw(GuiGraphics ctx, int x, int y, int width) {
        int barHeight = 4;
        int handleSize = 6;
        int barY = y + 2;  // center bar vertically in the handle area

        // Draw full gray background bar
        ctx.fill(x, barY, x + width, barY + barHeight, COLOR_BG);

        // Calculate how far to fill based on current value
        // pct = 0.0 at min, 1.0 at max
        float pct = (setting.value - setting.min) / (setting.max - setting.min);
        int fillWidth = (int) (width * pct);

        // Draw blue filled portion from left to current value
        if (fillWidth > 0) {
            ctx.fill(x, barY, x + fillWidth, barY + barHeight, COLOR_FILL);
        }

        // Draw white handle centered at current value position
        int handleX = x + fillWidth - (handleSize / 2);
        ctx.fill(handleX, y, handleX + handleSize, y + handleSize, COLOR_HANDLE);

        // Draw label above the bar: "SettingName: CurrentValue"
        // Format: one decimal place (e.g. "Reach: 6.0", "Size: 0.5")
        String label = setting.name + ": " + String.format("%.1f", setting.value);
        ctx.drawTextWithShadow(Minecraft.getInstance().textRenderer,
                label, x, y - 10, COLOR_TEXT);
    }

    /**
     * Call this when the mouse button is pressed.
     * Checks if the click is within the slider area and starts dragging.
     */
    public void onMouseClick(int mouseX, int mouseY, int sliderX, int sliderY, int sliderWidth) {
        // Clickable area: the full width, 8px tall (handle height)
        if (mouseX >= sliderX && mouseX <= sliderX + sliderWidth
                && mouseY >= sliderY && mouseY <= sliderY + 8) {
            dragging = true;
            // Update value immediately on click (don't wait for drag)
            updateValue(mouseX, sliderX, sliderWidth);
        }
    }

    /**
     * Call this every frame while the mouse button is held.
     * Only does anything if we started dragging on this slider.
     */
    public void onMouseDrag(int mouseX, int sliderX, int sliderWidth) {
        if (!dragging) return;
        updateValue(mouseX, sliderX, sliderWidth);
    }

    /**
     * Call this when the mouse button is released.
     */
    public void onMouseRelease() {
        dragging = false;
    }

    /**
     * Calculates and sets the new value from mouse X position.
     * Maps mouseX position to the setting's min-max range.
     */
    private void updateValue(int mouseX, int sliderX, int sliderWidth) {
        // Convert mouse position to 0.0-1.0 percentage
        float pct = (float) (mouseX - sliderX) / sliderWidth;

        // Clamp to valid range
        pct = Math.max(0.0f, Math.min(1.0f, pct));

        // Map percentage to setting range
        // e.g. for Reach (min=3.0, max=100.0): pct=0.5 -> value=51.5
        setting.value = setting.min + (setting.max - setting.min) * pct;
    }

    public boolean isDragging() {
        return dragging;
    }
}
