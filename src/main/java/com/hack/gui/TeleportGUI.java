package com.hack.gui;

import com.hack.modules.utility.Teleport;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.network.chat.Component;

/**
 * TeleportGUI - Enter X Y Z, press the big Teleport button to go there.
 *
 * LAYOUT (280 x 210 panel, centered on screen):
 *
 *   ╔══════════════════════════════════╗
 *   ║  ▶ TELEPORT                      ║  ← green title bar with accent
 *   ╠══════════════════════════════════╣
 *   ║  Current position shown here     ║  ← dim info line
 *   ║                                  ║
 *   ║   X  [ -128.50                 ] ║
 *   ║   Y  [   64.00                 ] ║
 *   ║   Z  [  256.00                 ] ║
 *   ║                                  ║
 *   ║  ┌────────────────────────────┐  ║
 *   ║  │       TELEPORT             │  ║  ← big green button
 *   ║  └────────────────────────────┘  ║
 *   ║  ┌────────────────────────────┐  ║
 *   ║  │        Cancel              │  ║  ← smaller grey button
 *   ║  └────────────────────────────┘  ║
 *   ╚══════════════════════════════════╝
 *
 * Tab  = cycle X → Y → Z → X
 * Enter = teleport
 * Esc   = cancel
 */
public class TeleportGUI extends Screen {

    private final Teleport teleportModule;

    private TextFieldWidget xField;
    private TextFieldWidget yField;
    private TextFieldWidget zField;

    private String errorMessage = "";

    // Panel dimensions — taller to fit the big button comfortably
    private static final int PANEL_W = 280;
    private static final int PANEL_H = 210;

    // Colors
    private static final int C_PANEL_BG   = 0xFF0D0D1A;  // very dark navy
    private static final int C_TITLE_BAR  = 0xFF0A1A0A;  // dark green tint
    private static final int C_BORDER     = 0xFF00FF7F;  // green border
    private static final int C_TITLE_TEXT = 0xFF00FF7F;  // green
    private static final int C_LABEL      = 0xFF888888;  // muted grey
    private static final int C_INFO       = 0xFF444466;  // very dim
    private static final int C_FIELD_BG   = 0xFF111122;  // dark field background
    private static final int C_FIELD_BDR  = 0xFF223322;  // subtle field border
    private static final int C_TP_BTN_BG  = 0xFF00CC66;  // solid green teleport button
    private static final int C_TP_BTN_TXT = 0xFF000000;  // black text on green
    private static final int C_CX_BTN_BG  = 0xFF1A1A2E;  // dark cancel button
    private static final int C_CX_BTN_TXT = 0xFF888888;  // grey cancel text
    private static final int C_ERROR      = 0xFFFF4444;  // red error
    private static final int C_ACCENT     = 0xFF00FF7F;

    public TeleportGUI(Teleport teleportModule) {
        super(Component.literal("Teleport"));
        this.teleportModule = teleportModule;
    }

    @Override
    protected void init() {
        int cx     = width  / 2;
        int cy     = height / 2;
        int px     = cx - PANEL_W / 2;
        int py     = cy - PANEL_H / 2;

        int labelW = 14;
        int fieldX = px + 40;
        int fieldW = PANEL_W - 52;

        // X field
        xField = new TextFieldWidget(textRenderer,
                fieldX, py + 52, fieldW, 16, Component.literal("X"));
        xField.setMaxLength(20);
        xField.setText(String.format("%.2f", teleportModule.targetX));
        xField.setChangedListener(s -> errorMessage = "");
        addDrawableChild(xField);

        // Y field
        yField = new TextFieldWidget(textRenderer,
                fieldX, py + 78, fieldW, 16, Component.literal("Y"));
        yField.setMaxLength(20);
        yField.setText(String.format("%.2f", teleportModule.targetY));
        yField.setChangedListener(s -> errorMessage = "");
        addDrawableChild(yField);

        // Z field
        zField = new TextFieldWidget(textRenderer,
                fieldX, py + 104, fieldW, 16, Component.literal("Z"));
        zField.setMaxLength(20);
        zField.setText(String.format("%.2f", teleportModule.targetZ));
        zField.setChangedListener(s -> errorMessage = "");
        addDrawableChild(zField);

        // Big TELEPORT button — full width minus padding, 24px tall
        addDrawableChild(ButtonWidget.builder(
                Component.literal("TELEPORT"), btn -> doTeleport())
                .dimensions(px + 12, py + 136, PANEL_W - 24, 24)
                .build());

        // Smaller Cancel button below it
        addDrawableChild(ButtonWidget.builder(
                Component.literal("Cancel"), btn -> close())
                .dimensions(px + 12, py + 168, PANEL_W - 24, 18)
                .build());

        setFocused(xField);
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        // Darkened game world behind
        renderBackground(ctx, mouseX, mouseY, delta);

        int cx = width  / 2;
        int cy = height / 2;
        int px = cx - PANEL_W / 2;
        int py = cy - PANEL_H / 2;

        // ── Outer panel background ─────────────────────────────
        ctx.fill(px, py, px + PANEL_W, py + PANEL_H, C_PANEL_BG);

        // ── Title bar (top 28px) ───────────────────────────────
        ctx.fill(px, py, px + PANEL_W, py + 28, C_TITLE_BAR);

        // Green left accent stripe in title bar
        ctx.fill(px, py, px + 3, py + 28, C_ACCENT);

        // Arrow icon drawn as two small filled rects (like ">")
        ctx.fill(px + 10, py + 10,  px + 13, py + 18, C_ACCENT); // vertical bar
        ctx.fill(px + 13, py + 12,  px + 16, py + 16, C_ACCENT); // arrow tip top
        ctx.fill(px + 13, py + 12,  px + 16, py + 20, C_ACCENT); // actually draw real >

        // Title text
        ctx.drawTextWithShadow(textRenderer, "TELEPORT",
                px + 20, py + 10, C_TITLE_TEXT);

        // ── Separator line under title ─────────────────────────
        ctx.fill(px, py + 28, px + PANEL_W, py + 29, 0xFF001A00);

        // ── Info line: current position ───────────────────────
        String info = String.format("Current: %.0f, %.0f, %.0f",
                teleportModule.targetX, teleportModule.targetY, teleportModule.targetZ);
        ctx.drawTextWithShadow(textRenderer, info, px + 12, py + 36, C_INFO);

        // ── Field labels ──────────────────────────────────────
        ctx.drawTextWithShadow(textRenderer, "X", px + 24, py + 55, C_LABEL);
        ctx.drawTextWithShadow(textRenderer, "Y", px + 24, py + 81, C_LABEL);
        ctx.drawTextWithShadow(textRenderer, "Z", px + 24, py + 107, C_LABEL);

        // ── Outer border (4 sides, 1px) ────────────────────────
        ctx.fill(px,              py,              px + PANEL_W, py + 1,           C_BORDER);
        ctx.fill(px,              py + PANEL_H - 1, px + PANEL_W, py + PANEL_H,   C_BORDER);
        ctx.fill(px,              py,              px + 1,        py + PANEL_H,    C_BORDER);
        ctx.fill(px + PANEL_W - 1, py,             px + PANEL_W, py + PANEL_H,    C_BORDER);

        // ── Corner dots for style ──────────────────────────────
        ctx.fill(px + 1, py + 1, px + 3, py + 3, C_ACCENT);
        ctx.fill(px + PANEL_W - 3, py + 1, px + PANEL_W - 1, py + 3, C_ACCENT);
        ctx.fill(px + 1, py + PANEL_H - 3, px + 3, py + PANEL_H - 1, C_ACCENT);
        ctx.fill(px + PANEL_W - 3, py + PANEL_H - 3, px + PANEL_W - 1, py + PANEL_H - 1, C_ACCENT);

        // ── Separator above buttons ────────────────────────────
        ctx.fill(px + 8, py + 128, px + PANEL_W - 8, py + 129, 0xFF111133);

        // ── Error message ──────────────────────────────────────
        if (!errorMessage.isEmpty()) {
            int ew = textRenderer.getWidth(errorMessage);
            ctx.drawTextWithShadow(textRenderer, errorMessage,
                    cx - ew / 2, py + 194, C_ERROR);
        }

        // Draw widgets (text fields + buttons) on top
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.getKeycode();
        // Tab cycles X → Y → Z → X
        if (keyCode == 258) {
            if      (getFocused() == xField) setFocused(yField);
            else if (getFocused() == yField) setFocused(zField);
            else                             setFocused(xField);
            return true;
        }
        // Enter or numpad Enter = teleport
        if (keyCode == 257 || keyCode == 335) {
            doTeleport();
            return true;
        }
        // Escape = close
        if (keyCode == 256) {
            close();
            return true;
        }
        return super.keyPressed(input);
    }

    private void doTeleport() {
        try {
            double x = Double.parseDouble(xField.getText().trim());
            double y = Double.parseDouble(yField.getText().trim());
            double z = Double.parseDouble(zField.getText().trim());

            teleportModule.targetX = x;
            teleportModule.targetY = y;
            teleportModule.targetZ = z;
            teleportModule.executeTeleport();

            close();
        } catch (NumberFormatException e) {
            errorMessage = "Invalid! Enter numbers only  (e.g. 128, -64.5)";
        }
    }

    @Override
    public boolean shouldPause() { return false; }
}
