package com.hack.modules;

import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

/**
 * HackModule — base class for every hack.
 *
 * keybind = -1 means no bind assigned.
 * Use right-click in the GUI or .bind in chat to assign.
 */
public abstract class HackModule {

    private String  name;
    private String  category;
    private boolean enabled  = false;
    private int     keybind  = -1;

    protected List<Setting> settings           = new ArrayList<>();
    protected boolean       alwaysShowSettings = false;

    public HackModule(String name, String category) {
        this.name     = name;
        this.category = category;
    }

    public void onTick()    {}
    public void onEnable()  {}
    public void onDisable() {}

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
    }

    // -----------------------------------------------
    // SETTING (slider)
    // -----------------------------------------------

    public static class Setting {
        public String name;
        public float  value, min, max;
        public Setting(String name, float value, float min, float max) {
            this.name = name; this.value = value; this.min = min; this.max = max;
        }
    }

    // -----------------------------------------------
    // GETTERS / SETTERS
    // -----------------------------------------------

    public boolean       isEnabled()            { return enabled; }
    public String        getName()              { return name; }
    public String        getCategory()          { return category; }
    public int           getKeybind()           { return keybind; }
    public void          setKeybind(int key)    { this.keybind = key; }
    public List<Setting> getSettings()          { return settings; }
    public boolean       isAlwaysShowSettings() { return alwaysShowSettings; }

    /**
     * Returns a short display label for the bound key, e.g. "[F]", "[F5]", "[UP]".
     * Returns "" if no bind is set.
     *
     * glfwGetKeyName() only works for printable keys (letters, numbers, symbols).
     * For everything else (function keys, arrows, specials) we use our own map.
     */
    public String getKeybindName() {
        if (keybind == -1) return "";
        String label = friendlyKeyName(keybind);
        return "[" + label + "]";
    }

    /** Human-readable name for any GLFW key code. */
    public static String friendlyKeyName(int key) {
        // Try GLFW's own name first — works for A-Z, 0-9, symbols
        String glfwName = GLFW.glfwGetKeyName(key, 0);
        if (glfwName != null && !glfwName.isEmpty()) {
            return glfwName.toUpperCase();
        }
        // Fall back to our map for non-printable keys
        switch (key) {
            case GLFW.GLFW_KEY_SPACE:         return "SPACE";
            case GLFW.GLFW_KEY_ENTER:         return "ENTER";
            case GLFW.GLFW_KEY_TAB:           return "TAB";
            case GLFW.GLFW_KEY_BACKSPACE:     return "BKSP";
            case GLFW.GLFW_KEY_ESCAPE:        return "ESC";
            case GLFW.GLFW_KEY_INSERT:        return "INS";
            case GLFW.GLFW_KEY_DELETE:        return "DEL";
            case GLFW.GLFW_KEY_HOME:          return "HOME";
            case GLFW.GLFW_KEY_END:           return "END";
            case GLFW.GLFW_KEY_PAGE_UP:       return "PGUP";
            case GLFW.GLFW_KEY_PAGE_DOWN:     return "PGDN";
            case GLFW.GLFW_KEY_UP:            return "UP";
            case GLFW.GLFW_KEY_DOWN:          return "DOWN";
            case GLFW.GLFW_KEY_LEFT:          return "LEFT";
            case GLFW.GLFW_KEY_RIGHT:         return "RIGHT";
            case GLFW.GLFW_KEY_LEFT_SHIFT:    return "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_SHIFT:   return "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL:  return "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL: return "RCTRL";
            case GLFW.GLFW_KEY_LEFT_ALT:      return "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT:     return "RALT";
            case GLFW.GLFW_KEY_F1:  return "F1";  case GLFW.GLFW_KEY_F2:  return "F2";
            case GLFW.GLFW_KEY_F3:  return "F3";  case GLFW.GLFW_KEY_F4:  return "F4";
            case GLFW.GLFW_KEY_F5:  return "F5";  case GLFW.GLFW_KEY_F6:  return "F6";
            case GLFW.GLFW_KEY_F7:  return "F7";  case GLFW.GLFW_KEY_F8:  return "F8";
            case GLFW.GLFW_KEY_F9:  return "F9";  case GLFW.GLFW_KEY_F10: return "F10";
            case GLFW.GLFW_KEY_F11: return "F11"; case GLFW.GLFW_KEY_F12: return "F12";
            case GLFW.GLFW_KEY_KP_0: return "NUM0"; case GLFW.GLFW_KEY_KP_1: return "NUM1";
            case GLFW.GLFW_KEY_KP_2: return "NUM2"; case GLFW.GLFW_KEY_KP_3: return "NUM3";
            case GLFW.GLFW_KEY_KP_4: return "NUM4"; case GLFW.GLFW_KEY_KP_5: return "NUM5";
            case GLFW.GLFW_KEY_KP_6: return "NUM6"; case GLFW.GLFW_KEY_KP_7: return "NUM7";
            case GLFW.GLFW_KEY_KP_8: return "NUM8"; case GLFW.GLFW_KEY_KP_9: return "NUM9";
            case GLFW.GLFW_KEY_KP_ENTER:  return "NUMENT";
            case GLFW.GLFW_KEY_KP_ADD:    return "NUM+";
            case GLFW.GLFW_KEY_KP_SUBTRACT: return "NUM-";
            case GLFW.GLFW_KEY_KP_MULTIPLY: return "NUM*";
            case GLFW.GLFW_KEY_KP_DIVIDE:   return "NUM/";
            default: return "KEY" + key;
        }
    }
}
