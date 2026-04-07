package com.hack.gui;

import com.hack.ModuleManager;
import com.hack.gui.components.Slider;
import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;



import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ClickGUI — main hack menu, opened/closed with G.
 *
 * LAYOUT:
 *   ┌────────────────────────────────────────────────┐
 *   │  HackClient          [18 modules]              │  header bar
 *   │  [S] Search modules...                         │  search bar
 *   │  [All][Movement][Combat][Visual][Utility]       │  filter tabs
 *   ├────────────────────────────────────────────────┤
 *   │  Columns per category (or flat list if search) │
 *   └────────────────────────────────────────────────┘
 *
 * CONTROLS:
 *   Left-click module   = toggle on/off
 *   Right-click module  = enter keybind listening (yellow row, press any key)
 *                         right-click again while yellow = unbind
 *   Middle-click module = instant unbind
 *   Drag slider         = adjust value
 *   Esc                 = close (or cancel listening)
 */
public class ClickGUI extends Screen {

    // ── filter state ───────────────────────────────────────────
    private final Minecraft mc = Minecraft.getInstance();
    private EditBox searchField;
    private String activeCategory = null;
    private static final List<String> CATS = List.of("Movement","Combat","Visual","Utility");

    // ── keybind listening ──────────────────────────────────────
    private String listeningModule = null;

    // ── slider state (persists across filter changes) ──────────
    private final Set<String>         expanded = new HashSet<>();
    private final Map<String, Slider> sliders  = new HashMap<>();

    // ── layout ────────────────────────────────────────────────
    private static final int HDR_H    = 22;   // top header bar
    private static final int SRCH_Y   = HDR_H + 4;
    private static final int SRCH_H   = 15;
    private static final int TAB_Y    = HDR_H + SRCH_H + 8;
    private static final int TAB_H    = 13;
    private static final int MODS_Y   = HDR_H + SRCH_H + TAB_H + 14;
    private static final int MOD_H    = 14;
    private static final int SLD_H    = 24;
    private static final int PAD      = 6;
    private static final int COL_W    = 130;

    // ── colors ────────────────────────────────────────────────
    private static final int C_HDR       = 0xFF090914;
    private static final int C_ACCENT    = 0xFF00FF7F;
    private static final int C_SRCH_BG   = 0xFF0D0D1A;
    private static final int C_TAB_ON    = 0xFF00FF7F;
    private static final int C_TAB_OFF   = 0xFF111122;
    private static final int C_TAB_HOV   = 0xFF1A1A33;
    private static final int C_MOD_ON    = 0xFF0F1F0F;
    private static final int C_MOD_OFF   = 0xFF0A0A14;
    private static final int C_MOD_HOV   = 0xFF141424;
    private static final int C_LISTEN    = 0xFF1A1400;
    private static final int C_SLD_BG    = 0xFF080810;
    private static final int C_COL_HDR   = 0xFF0B0B18;
    private static final int C_TXT_ON    = 0xFF00FF7F;
    private static final int C_TXT_OFF   = 0xFF666677;
    private static final int C_TXT_HOV   = 0xFF8888AA;
    private static final int C_BIND_TXT  = 0xFF3377FF;
    private static final int C_LISTEN_TXT= 0xFFFFCC00;
    private static final int C_DIM       = 0xFF2A2A3A;
    private static final int C_WHITE     = 0xFFDDDDEE;
    private static final int C_MUTED     = 0xFF444455;
    private static final int C_OVERLAY   = 0xCC070710;

    public ClickGUI() { super(Component.literal("CrownDrip Client")); }

    @Override
    protected void init() {
        int sw = width;
        searchField = new EditBox(
                textRenderer,
                PAD + 14, SRCH_Y + 1,
                sw - PAD * 2 - 16, SRCH_H - 2,
                Component.literal("Search"));
        searchField.setMaxLength(40);
        searchField.setPlaceholder(Component.literal("Search modules..."));
        addDrawableChild(searchField);

        for (HackModule m : ModuleManager.getAllModules()) {
            for (HackModule.Setting s : m.getSettings()) {
                String k = m.getName() + "." + s.name;
                if (!sliders.containsKey(k)) sliders.put(k, new Slider(s));
            }
        }
        setFocused(searchField);
    }

    // ── helpers ────────────────────────────────────────────────

    private String query() {
        return searchField == null ? "" : searchField.getText().trim();
    }

    private List<HackModule> filtered() {
        String q = query().toLowerCase();
        return ModuleManager.getAllModules().stream()
                .filter(m -> activeCategory == null || m.getCategory().equals(activeCategory))
                .filter(m -> q.isEmpty()
                        || m.getName().toLowerCase().contains(q)
                        || m.getCategory().toLowerCase().contains(q))
                .collect(Collectors.toList());
    }

    // ── render ─────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics ctx, int mx, int my, float delta) {
        int sw = width;

        // Full-screen dark overlay
        ctx.fill(0, 0, sw, height, C_OVERLAY);

        // ── Header bar ────────────────────────────────────────
        ctx.fill(0, 0, sw, HDR_H, C_HDR);
        // Green left accent
        ctx.fill(0, 0, 3, HDR_H, C_ACCENT);
        // Title
        ctx.drawTextWithShadow(textRenderer, "CrownDrip Client", 8, 7, C_ACCENT);
        // Module count on right
        int total = ModuleManager.getAllModules().size();
        int active = (int) ModuleManager.getAllModules().stream().filter(HackModule::isEnabled).count();
        String countStr = active + "/" + total + " active";
        int cw = textRenderer.getWidth(countStr);
        ctx.drawTextWithShadow(textRenderer, countStr, sw - cw - 8, 7, C_MUTED);
        // Thin green line at bottom of header
        ctx.fill(0, HDR_H - 1, sw, HDR_H, 0xFF003300);

        // ── Search bar ────────────────────────────────────────
        ctx.fill(PAD, SRCH_Y, sw - PAD, SRCH_Y + SRCH_H, C_SRCH_BG);
        // Search icon (S letter styled)
        ctx.drawTextWithShadow(textRenderer, "S", PAD + 3, SRCH_Y + 3, C_MUTED);
        // Bottom border on search
        ctx.fill(PAD, SRCH_Y + SRCH_H, sw - PAD, SRCH_Y + SRCH_H + 1,
                searchField.isFocused() ? C_ACCENT : C_DIM);

        // ── Category tabs ─────────────────────────────────────
        drawTabs(ctx, sw, mx, my);

        // ── Thin separator ────────────────────────────────────
        ctx.fill(0, MODS_Y - 2, sw, MODS_Y - 1, 0xFF0F0F1F);

        // ── Module list ───────────────────────────────────────
        drawModules(ctx, mx, my);

        // Draw child widgets (search field) on top
        super.render(ctx, mx, my, delta);
    }

    private void drawTabs(GuiGraphics ctx, int sw, int mx, int my) {
        List<String> tabs = new ArrayList<>();
        tabs.add("All");
        tabs.addAll(CATS);

        int n    = tabs.size();
        int btnW = (sw - PAD * 2 - (n - 1) * 3) / n;
        int bx   = PAD;

        for (String tab : tabs) {
            boolean on  = tab.equals("All") ? activeCategory == null : tab.equals(activeCategory);
            boolean hov = !on && mx >= bx && mx <= bx + btnW
                    && my >= TAB_Y && my <= TAB_Y + TAB_H;

            int bg  = on ? C_TAB_ON : (hov ? C_TAB_HOV : C_TAB_OFF);
            int txt = on ? 0xFF000000 : (hov ? C_TXT_HOV : C_TXT_OFF);

            ctx.fill(bx, TAB_Y, bx + btnW, TAB_Y + TAB_H, bg);

            // Active tab gets a brighter top edge
            if (on) ctx.fill(bx, TAB_Y, bx + btnW, TAB_Y + 1, C_ACCENT);

            int tw = textRenderer.getWidth(tab);
            ctx.drawTextWithShadow(textRenderer, tab,
                    bx + (btnW - tw) / 2, TAB_Y + 3, txt);

            bx += btnW + 3;
        }
    }

    private void drawModules(GuiGraphics ctx, int mx, int my) {
        List<HackModule> list = filtered();
        int sw = width;

        if (list.isEmpty()) {
            String msg = "No results for  \"" + query() + "\"";
            ctx.drawCenteredTextWithShadow(textRenderer, msg,
                    sw / 2, MODS_Y + 16, C_MUTED);
            return;
        }

        if (!query().isEmpty()) {
            drawFlat(ctx, mx, my, list);
        } else {
            drawColumns(ctx, mx, my, list);
        }
    }

    private void drawFlat(GuiGraphics ctx, int mx, int my, List<HackModule> list) {
        int sw   = width;
        int w    = Math.min(COL_W + 20, sw - PAD * 2);
        int x    = (sw - w) / 2;
        int y    = MODS_Y;
        String lastCat = null;

        for (HackModule m : list) {
            if (!m.getCategory().equals(lastCat)) {
                lastCat = m.getCategory();
                // Small category label
                ctx.fill(x, y, x + w, y + 10, C_COL_HDR);
                ctx.fill(x, y, x + 2, y + 10, C_ACCENT);
                ctx.drawTextWithShadow(textRenderer, lastCat,
                        x + 5, y + 1, C_MUTED);
                y += 10;
            }
            y = drawRow(ctx, mx, my, m, x, y, w);
        }
    }

    private void drawColumns(GuiGraphics ctx, int mx, int my, List<HackModule> list) {
        int sw = width;
        List<String> visCats = CATS.stream()
                .filter(c -> list.stream().anyMatch(m -> m.getCategory().equals(c)))
                .collect(Collectors.toList());
        if (visCats.isEmpty()) return;

        int gap    = 4;
        int cols   = visCats.size();
        int w      = Math.min(COL_W, (sw - PAD * 2 - gap * (cols - 1)) / cols);
        int totalW = w * cols + gap * (cols - 1);
        int sx     = (sw - totalW) / 2;

        for (int ci = 0; ci < cols; ci++) {
            String cat = visCats.get(ci);
            int cx = sx + ci * (w + gap);
            int y  = MODS_Y;

            // Column header
            ctx.fill(cx, y, cx + w, y + 13, C_COL_HDR);
            ctx.fill(cx, y, cx + 2, y + 13, C_ACCENT);
            ctx.drawTextWithShadow(textRenderer, cat, cx + 5, y + 3, C_ACCENT);
            y += 13;

            for (HackModule m : list) {
                if (!m.getCategory().equals(cat)) continue;
                y = drawRow(ctx, mx, my, m, cx, y, w);
            }
        }
    }

    private int drawRow(GuiGraphics ctx, int mx, int my,
                         HackModule m, int x, int y, int w) {
        boolean on        = m.isEnabled();
        boolean listening = m.getName().equals(listeningModule);
        boolean hover     = !listening && mx >= x && mx <= x + w
                && my >= y && my <= y + MOD_H;
        boolean alwaysOpen  = m.isAlwaysShowSettings();
        boolean showSliders = alwaysOpen || expanded.contains(m.getName());

        // Row background
        int bg = listening ? C_LISTEN : on ? C_MOD_ON : (hover ? C_MOD_HOV : C_MOD_OFF);
        ctx.fill(x, y, x + w, y + MOD_H, bg);

        // Green left accent when on
        if (on)  ctx.fill(x, y, x + 2, y + MOD_H, C_ACCENT);
        // Yellow left accent when listening
        if (listening) ctx.fill(x, y, x + 2, y + MOD_H, C_LISTEN_TXT);

        // Module name
        int nc = listening ? C_LISTEN_TXT : on ? C_TXT_ON : (hover ? C_TXT_HOV : C_TXT_OFF);
        ctx.drawTextWithShadow(textRenderer, m.getName(), x + 5, y + 3, nc);

        // Right side
        if (listening) {
            String hint = "key...";
            ctx.drawTextWithShadow(textRenderer, hint,
                    x + w - textRenderer.getWidth(hint) - 3, y + 3, C_LISTEN_TXT);
        } else {
            String bind = m.getKeybindName();
            if (!bind.isEmpty()) {
                ctx.drawTextWithShadow(textRenderer, bind,
                        x + w - textRenderer.getWidth(bind) - 3, y + 3, C_BIND_TXT);
            } else if (m.getSettings().size() > 0 && !alwaysOpen) {
                String arr = expanded.contains(m.getName()) ? "-" : "+";
                ctx.drawTextWithShadow(textRenderer, arr, x + w - 9, y + 3, C_DIM);
            }
        }

        y += MOD_H;

        // Sliders
        if (showSliders && m.getSettings().size() > 0) {
            for (HackModule.Setting s : m.getSettings()) {
                ctx.fill(x, y, x + w, y + SLD_H, C_SLD_BG);
                // Slider label + value
                String lbl = s.name + ": " + String.format("%.1f", s.value);
                ctx.drawTextWithShadow(textRenderer, lbl, x + 5, y + 3, C_MUTED);
                // Actual slider widget drawn by Slider.draw()
                Slider sl = sliders.get(m.getName() + "." + s.name);
                if (sl != null) sl.draw(ctx, x + 5, y + SLD_H - 8, w - 10);
                y += SLD_H;
            }
        }

        return y;
    }

    // ── key events ─────────────────────────────────────────────

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        int keyCode = input.getKeycode();
        if (listeningModule != null) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                listeningModule = null;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                HackModule m = findMod(listeningModule);
                if (m != null) m.setKeybind(-1);
                listeningModule = null;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_G) return true; // reserved
            HackModule m = findMod(listeningModule);
            if (m != null) m.setKeybind(keyCode);
            listeningModule = null;
            return true;
        }
        // G key closes the GUI (same key that opens it)
        if (keyCode == GLFW.GLFW_KEY_G) { close(); return true; }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        // Forward everything else to search field
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchField != null) return searchField.charTyped(input);
        return false;
    }

    // ── mouse events ───────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int btn = click.button();
        double mx = mc.mouse.getX() * mc.getWindow().getScaledWidth() / mc.getWindow().getWidth();
        double my = mc.mouse.getY() * mc.getWindow().getScaledHeight() / mc.getWindow().getHeight();
        int ix = (int) mx, iy = (int) my;

        // Tab clicks
        if (iy >= TAB_Y && iy <= TAB_Y + TAB_H) {
            clickTab(ix);
            return true;
        }
        // Module clicks
        if (iy >= MODS_Y) {
            clickModules(ix, iy, btn);
            return true;
        }
        return super.mouseClicked(click, inside);
    }

    private void clickTab(int mx) {
        int sw = width;
        List<String> tabs = new ArrayList<>(); tabs.add("All"); tabs.addAll(CATS);
        int n = tabs.size();
        int btnW = (sw - PAD * 2 - (n - 1) * 3) / n;
        int bx = PAD;
        for (String t : tabs) {
            if (mx >= bx && mx <= bx + btnW) {
                activeCategory = t.equals("All") ? null : t;
                return;
            }
            bx += btnW + 3;
        }
    }

    private void clickModules(int mx, int my, int btn) {
        List<HackModule> list = filtered();
        int sw = width;

        if (!query().isEmpty()) {
            // Flat list
            int w = Math.min(COL_W + 20, sw - PAD * 2);
            int x = (sw - w) / 2, y = MODS_Y;
            String lastCat = null;
            for (HackModule m : list) {
                if (!m.getCategory().equals(lastCat)) { lastCat = m.getCategory(); y += 10; }
                y = clickRow(mx, my, btn, m, x, y, w);
            }
        } else {
            // Column layout
            List<String> visCats = CATS.stream()
                    .filter(c -> list.stream().anyMatch(m -> m.getCategory().equals(c)))
                    .collect(Collectors.toList());
            int n = visCats.size();
            int gap = 4;
            int w = Math.min(COL_W, (sw - PAD * 2 - gap * (n - 1)) / n);
            int sx = (sw - (w * n + gap * (n - 1))) / 2;
            for (int ci = 0; ci < n; ci++) {
                String cat = visCats.get(ci);
                int cx = sx + ci * (w + gap), y = MODS_Y + 13;
                for (HackModule m : list) {
                    if (!m.getCategory().equals(cat)) continue;
                    y = clickRow(mx, my, btn, m, cx, y, w);
                }
            }
        }
    }

    private int clickRow(int mx, int my, int btn, HackModule m, int x, int y, int w) {
        boolean alwaysOpen  = m.isAlwaysShowSettings();
        boolean showSliders = alwaysOpen || expanded.contains(m.getName());

        if (mx >= x && mx <= x + w && my >= y && my <= y + MOD_H) {
            if (btn == 0) {
                // Left click = toggle
                if (m.getName().equals(listeningModule)) listeningModule = null;
                // Teleport opens its GUI directly; all others toggle
                if (m.getName().equals("Teleport")) {
                    com.hack.modules.utility.Teleport tp =
                            ModuleManager.get(com.hack.modules.utility.Teleport.class);
                    if (tp != null) {
                        tp.prefillCurrentPosition();
                        Minecraft.getInstance().setScreen(new TeleportGUI(tp));
                    }
                } else {
                    m.toggle();
                }
            } else if (btn == 1) {
                // Right click:
                // - if already listening → cancel listen + unbind
                // - if has expandable sliders → toggle expand (separate from listen)
                // - otherwise → enter keybind listen mode
                if (m.getName().equals(listeningModule)) {
                    m.setKeybind(-1); listeningModule = null;
                } else if (!alwaysOpen && m.getSettings().size() > 0) {
                    // Toggle slider expand only — right-click = expand, not listen
                    if (expanded.contains(m.getName())) expanded.remove(m.getName());
                    else expanded.add(m.getName());
                } else {
                    listeningModule = m.getName();
                }
            } else if (btn == 2) {
                m.setKeybind(-1);
                if (m.getName().equals(listeningModule)) listeningModule = null;
            }
        }
        y += MOD_H;

        if (showSliders) {
            for (HackModule.Setting s : m.getSettings()) {
                Slider sl = sliders.get(m.getName() + "." + s.name);
                if (sl != null) sl.onMouseClick(mx, my, x + 5, y + SLD_H - 8, w - 10);
                y += SLD_H;
            }
        }
        return y;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        double mx = mc.mouse.getX() * mc.getWindow().getScaledWidth() / mc.getWindow().getWidth();
        double my = mc.mouse.getY() * mc.getWindow().getScaledHeight() / mc.getWindow().getHeight();
        List<HackModule> list = filtered();
        int sw = width;
        if (!query().isEmpty()) {
            int w = Math.min(COL_W + 20, sw - PAD * 2), x = (sw - w) / 2;
            for (HackModule m : list) {
                boolean show = m.isAlwaysShowSettings() || expanded.contains(m.getName());
                if (show) for (HackModule.Setting s : m.getSettings()) {
                    Slider sl = sliders.get(m.getName() + "." + s.name);
                    if (sl != null) sl.onMouseDrag((int) mx, x + 5, w - 10);
                }
            }
        } else {
            List<String> visCats = CATS.stream()
                    .filter(c -> list.stream().anyMatch(m -> m.getCategory().equals(c)))
                    .collect(Collectors.toList());
            int n = visCats.size(), gap = 4;
            int w = Math.min(COL_W, (sw - PAD * 2 - gap * (n - 1)) / n);
            int sx = (sw - (w * n + gap * (n - 1))) / 2;
            for (int ci = 0; ci < n; ci++) {
                int cx = sx + ci * (w + gap);
                for (HackModule m : list) {
                    if (!m.getCategory().equals(visCats.get(ci))) continue;
                    boolean show = m.isAlwaysShowSettings() || expanded.contains(m.getName());
                    if (show) for (HackModule.Setting s : m.getSettings()) {
                        Slider sl = sliders.get(m.getName() + "." + s.name);
                        if (sl != null) sl.onMouseDrag((int) mx, cx + 5, w - 10);
                    }
                }
            }
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        sliders.values().forEach(Slider::onMouseRelease);
        return super.mouseReleased(click);
    }

    private HackModule findMod(String name) {
        return ModuleManager.getAllModules().stream()
                .filter(m -> m.getName().equals(name)).findFirst().orElse(null);
    }

    @Override
    public boolean shouldPause() { return false; }
}
