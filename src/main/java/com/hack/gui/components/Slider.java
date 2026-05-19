package com.hack.gui.components;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Slider - A draggable slider widget for adjusting module settings.
 */
public class Slider {
    private final HackModule.Setting setting;
    private boolean dragging = false;

    private static final int COLOR_BG     = 0xFF333333;
    private static final int COLOR_FILL   = 0xFF0077FF;
    private static final int COLOR_HANDLE = 0xFFFFFFFF;
    private static final int COLOR_TEXT   = 0xFFAAAAAA;

    public Slider(HackModule.Setting setting) {
        this.setting = setting;
    }

    public void draw(GuiGraphics ctx, int x, int y, int width) {
        int barHeight = 4;
        int handleSize = 6;
        int barY = y + 2;

        ctx.fill(x, barY, x + width, barY + barHeight, COLOR_BG);

        float pct = (setting.value - setting.min) / (setting.max - setting.min);
        int fillWidth = (int) (width * pct);

        if (fillWidth > 0) {
            ctx.fill(x, barY, x + fillWidth, barY + barHeight, COLOR_FILL);
        }

        int handleX = x + fillWidth - (handleSize / 2);
        ctx.fill(handleX, y, handleX + handleSize, y + handleSize, COLOR_HANDLE);

        String label = setting.name + ": " + String.format("%.1f", setting.value);
        ctx.drawString(Minecraft.getInstance().font, label, x, y - 10, COLOR_TEXT, true);
    }

    public void onMouseClick(int mouseX, int mouseY, int sliderX, int sliderY, int sliderWidth) {
        if (mouseX >= sliderX && mouseX <= sliderX + sliderWidth
                && mouseY >= sliderY && mouseY <= sliderY + 8) {
            dragging = true;
            updateValue(mouseX, sliderX, sliderWidth);
        }
    }

    public void onMouseDrag(int mouseX, int sliderX, int sliderWidth) {
        if (!dragging) return;
        updateValue(mouseX, sliderX, sliderWidth);
    }

    public void onMouseRelease() {
        dragging = false;
    }

    private void updateValue(int mouseX, int sliderX, int sliderWidth) {
        float pct = (float) (mouseX - sliderX) / sliderWidth;
        pct = Math.max(0.0f, Math.min(1.0f, pct));
        setting.value = setting.min + (setting.max - setting.min) * pct;
    }

    public boolean isDragging() {
        return dragging;
    }
}
