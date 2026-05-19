package com.hack.gui;

import com.hack.modules.utility.Teleport;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * TeleportGUI - Enter X Y Z, press the big Teleport button to go there.
 */
public class TeleportGUI extends Screen {
    private final Teleport teleportModule;
    private EditBox xField;
    private EditBox yField;
    private EditBox zField;
    private String errorMessage = "";

    private static final int PANEL_W = 280;
    private static final int PANEL_H = 210;

    private static final int C_PANEL_BG   = 0xEE05050A;
    private static final int C_TITLE_BAR  = 0xFF0A0A14;
    private static final int C_ACCENT     = 0xFF00FF7F;
    private static final int C_TITLE_TEXT = 0xFFFFFFFF;
    private static final int C_INFO       = 0xFF888888;
    private static final int C_LABEL      = 0xFFCCCCCC;
    private static final int C_BORDER     = 0xFF1A1A33;
    private static final int C_ERROR      = 0xFFFF3333;

    public TeleportGUI(Teleport module) {
        super(Component.literal("Teleport"));
        this.teleportModule = module;
    }

    @Override
    protected void init() {
        int cx = width  / 2;
        int cy = height / 2;
        int px = cx - PANEL_W / 2;
        int py = cy - PANEL_H / 2;

        xField = new EditBox(this.font, px + 40, py + 50, 210, 18, Component.literal("X"));
        yField = new EditBox(this.font, px + 40, py + 76, 210, 18, Component.literal("Y"));
        zField = new EditBox(this.font, px + 40, py + 102, 210, 18, Component.literal("Z"));

        xField.setValue(String.format("%.2f", teleportModule.targetX));
        yField.setValue(String.format("%.2f", teleportModule.targetY));
        zField.setValue(String.format("%.2f", teleportModule.targetZ));

        this.addRenderableWidget(xField);
        this.addRenderableWidget(yField);
        this.addRenderableWidget(zField);

        this.addRenderableWidget(Button.builder(Component.literal("TELEPORT"), b -> doTeleport())
                .bounds(px + 20, py + 138, PANEL_W - 40, 24).build());

        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), b -> onClose())
                .bounds(px + 20, py + 166, PANEL_W - 40, 20).build());

        this.setFocused(xField);
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        this.renderBackground(ctx, mouseX, mouseY, delta);
        int cx = width  / 2;
        int cy = height / 2;
        int px = cx - PANEL_W / 2;
        int py = cy - PANEL_H / 2;

        ctx.fill(px, py, px + PANEL_W, py + PANEL_H, C_PANEL_BG);
        ctx.fill(px, py, px + PANEL_W, py + 28, C_TITLE_BAR);
        ctx.fill(px, py, px + 3, py + 28, C_ACCENT);

        ctx.drawString(this.font, "TELEPORT", px + 20, py + 10, C_TITLE_TEXT, true);

        String info = String.format("Current: %.0f, %.0f, %.0f",
                teleportModule.targetX, teleportModule.targetY, teleportModule.targetZ);
        ctx.drawString(this.font, info, px + 12, py + 36, C_INFO, true);

        ctx.drawString(this.font, "X", px + 24, py + 55, C_LABEL, true);
        ctx.drawString(this.font, "Y", px + 24, py + 81, C_LABEL, true);
        ctx.drawString(this.font, "Z", px + 24, py + 107, C_LABEL, true);

        if (!errorMessage.isEmpty()) {
            int ew = this.font.width(errorMessage);
            ctx.drawString(this.font, errorMessage, cx - ew / 2, py + 194, C_ERROR, true);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 258) { // Tab
            if      (getFocused() == xField) setFocused(yField);
            else if (getFocused() == yField) setFocused(zField);
            else                             setFocused(xField);
            return true;
        }
        if (keyCode == 257 || keyCode == 335) { // Enter
            doTeleport();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void doTeleport() {
        try {
            double x = Double.parseDouble(xField.getValue().trim());
            double y = Double.parseDouble(yField.getValue().trim());
            double z = Double.parseDouble(zField.getValue().trim());
            teleportModule.targetX = x;
            teleportModule.targetY = y;
            teleportModule.targetZ = z;
            teleportModule.executeTeleport();
            onClose();
        } catch (NumberFormatException e) {
            errorMessage = "Invalid! Enter numbers only";
        }
    }

    @Override
    public boolean shouldPause() { return false; }
}
