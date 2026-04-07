package com.hack.mixins;

import com.hack.HackClient;
import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ChatMixin - Intercepts outgoing chat to handle .bind commands.
 *
 * 1.21.11 FIX:
 * - sendChatMessage moved from LocalPlayer to ClientPacketListener
 * - Signature changed from (String, MessageSignatureData) to just (String)
 * - Mixin target is now ClientPacketListener instead of LocalPlayer
 *
 * USAGE:
 *   .bind Fly f         -> bind F key to Fly
 *   .bind Fly none      -> clear Fly's keybind
 *   .binds              -> list all current binds
 */
@Mixin(ClientPacketListener.class)
public class ChatMixin {

    @Inject(
        method = "sendChatMessage(Ljava/lang/String;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onSendMessage(String message, CallbackInfo ci) {
        if (!message.startsWith(".")) return;
        if (HackClient.moduleManager == null) return;

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        String lower = message.toLowerCase().trim();

        // .binds -> list all current keybinds
        if (lower.equals(".binds")) {
            ci.cancel();
            chat(player, "§a=== Current Keybinds ===");
            for (HackModule m : HackClient.moduleManager.getAllModules()) {
                String bind = m.getKeybind() == -1 ? "§7none" : "§e" + m.getKeybindName();
                chat(player, "§f" + m.getName() + " §8-> " + bind);
            }
            chat(player, "§7Type .bind [module] [key]  or  .bind [module] none");
            return;
        }

        // .bind [module] [key]
        if (!lower.startsWith(".bind ")) return;
        ci.cancel();

        String[] parts = message.trim().split("\\s+");
        if (parts.length != 3) {
            chat(player, "§cUsage: .bind [module] [key]");
            chat(player, "§cExample: .bind Fly f");
            chat(player, "§cTo remove: .bind Fly none");
            chat(player, "§cList all:  .binds");
            return;
        }

        String moduleName = parts[1];
        String keyName    = parts[2].toLowerCase();

        HackModule target = null;
        for (HackModule m : HackClient.moduleManager.getAllModules()) {
            if (m.getName().equalsIgnoreCase(moduleName)) { target = m; break; }
        }

        if (target == null) {
            chat(player, "§cModule not found: §f" + moduleName);
            chat(player, "§7Available: " + moduleList());
            return;
        }

        if (keyName.equals("none") || keyName.equals("clear") || keyName.equals("unbind")) {
            target.setKeybind(-1);
            chat(player, "§aRemoved keybind from §f" + target.getName());
            return;
        }

        int glfwKey = resolveKey(keyName);
        if (glfwKey == -1) {
            chat(player, "§cUnknown key: §f" + keyName);
            chat(player, "§7Keys: a-z, 0-9, f1-f12, space, enter, esc, ins, del,");
            chat(player, "§7      home, end, pgup, pgdn, up, down, left, right");
            return;
        }

        target.setKeybind(glfwKey);
        String friendly = com.hack.modules.HackModule.friendlyKeyName(glfwKey);
        chat(player, "§aBound §f" + target.getName() + " §ato §f[" + friendly + "]");
    }

    private void chat(LocalPlayer player, String text) {
        player.sendMessage(Component.literal(text), false);
    }

    private String moduleList() {
        StringBuilder sb = new StringBuilder();
        for (HackModule m : HackClient.moduleManager.getAllModules()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(m.getName());
        }
        return sb.toString();
    }

    private int resolveKey(String n) {
        switch (n) {
            case "a": return GLFW.GLFW_KEY_A; case "b": return GLFW.GLFW_KEY_B;
            case "c": return GLFW.GLFW_KEY_C; case "d": return GLFW.GLFW_KEY_D;
            case "e": return GLFW.GLFW_KEY_E; case "f": return GLFW.GLFW_KEY_F;
            case "h": return GLFW.GLFW_KEY_H; case "i": return GLFW.GLFW_KEY_I;
            case "j": return GLFW.GLFW_KEY_J; case "k": return GLFW.GLFW_KEY_K;
            case "l": return GLFW.GLFW_KEY_L; case "m": return GLFW.GLFW_KEY_M;
            case "n": return GLFW.GLFW_KEY_N; case "o": return GLFW.GLFW_KEY_O;
            case "p": return GLFW.GLFW_KEY_P; case "q": return GLFW.GLFW_KEY_Q;
            case "r": return GLFW.GLFW_KEY_R; case "s": return GLFW.GLFW_KEY_S;
            case "t": return GLFW.GLFW_KEY_T; case "u": return GLFW.GLFW_KEY_U;
            case "v": return GLFW.GLFW_KEY_V; case "w": return GLFW.GLFW_KEY_W;
            case "x": return GLFW.GLFW_KEY_X; case "y": return GLFW.GLFW_KEY_Y;
            case "z": return GLFW.GLFW_KEY_Z;
            case "0": return GLFW.GLFW_KEY_0; case "1": return GLFW.GLFW_KEY_1;
            case "2": return GLFW.GLFW_KEY_2; case "3": return GLFW.GLFW_KEY_3;
            case "4": return GLFW.GLFW_KEY_4; case "5": return GLFW.GLFW_KEY_5;
            case "6": return GLFW.GLFW_KEY_6; case "7": return GLFW.GLFW_KEY_7;
            case "8": return GLFW.GLFW_KEY_8; case "9": return GLFW.GLFW_KEY_9;
            case "f1":  return GLFW.GLFW_KEY_F1;  case "f2":  return GLFW.GLFW_KEY_F2;
            case "f3":  return GLFW.GLFW_KEY_F3;  case "f4":  return GLFW.GLFW_KEY_F4;
            case "f5":  return GLFW.GLFW_KEY_F5;  case "f6":  return GLFW.GLFW_KEY_F6;
            case "f7":  return GLFW.GLFW_KEY_F7;  case "f8":  return GLFW.GLFW_KEY_F8;
            case "f9":  return GLFW.GLFW_KEY_F9;  case "f10": return GLFW.GLFW_KEY_F10;
            case "f11": return GLFW.GLFW_KEY_F11; case "f12": return GLFW.GLFW_KEY_F12;
            case "space": case "spc":    return GLFW.GLFW_KEY_SPACE;
            case "enter": case "return": return GLFW.GLFW_KEY_ENTER;
            case "tab":                  return GLFW.GLFW_KEY_TAB;
            case "backspace": case "bs": return GLFW.GLFW_KEY_BACKSPACE;
            case "escape": case "esc":   return GLFW.GLFW_KEY_ESCAPE;
            case "insert": case "ins":   return GLFW.GLFW_KEY_INSERT;
            case "delete": case "del":   return GLFW.GLFW_KEY_DELETE;
            case "home":                 return GLFW.GLFW_KEY_HOME;
            case "end":                  return GLFW.GLFW_KEY_END;
            case "pageup":  case "pgup": return GLFW.GLFW_KEY_PAGE_UP;
            case "pagedown":case "pgdn": return GLFW.GLFW_KEY_PAGE_DOWN;
            case "up":    return GLFW.GLFW_KEY_UP;    case "down":  return GLFW.GLFW_KEY_DOWN;
            case "left":  return GLFW.GLFW_KEY_LEFT;  case "right": return GLFW.GLFW_KEY_RIGHT;
            case "shift": case "lshift": return GLFW.GLFW_KEY_LEFT_SHIFT;
            case "ctrl":  case "lctrl":  return GLFW.GLFW_KEY_LEFT_CONTROL;
            case "alt":   case "lalt":   return GLFW.GLFW_KEY_LEFT_ALT;
            case "rshift":               return GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "rctrl":                return GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "ralt":                 return GLFW.GLFW_KEY_RIGHT_ALT;
            case "num0": return GLFW.GLFW_KEY_KP_0; case "num1": return GLFW.GLFW_KEY_KP_1;
            case "num2": return GLFW.GLFW_KEY_KP_2; case "num3": return GLFW.GLFW_KEY_KP_3;
            case "num4": return GLFW.GLFW_KEY_KP_4; case "num5": return GLFW.GLFW_KEY_KP_5;
            case "num6": return GLFW.GLFW_KEY_KP_6; case "num7": return GLFW.GLFW_KEY_KP_7;
            case "num8": return GLFW.GLFW_KEY_KP_8; case "num9": return GLFW.GLFW_KEY_KP_9;
            default: return -1;
        }
    }
}
