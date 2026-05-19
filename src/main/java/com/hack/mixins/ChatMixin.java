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
 */
@Mixin(ClientPacketListener.class)
public class ChatMixin {
    @Inject(
        method = "sendChat",
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

        if (lower.equals(".binds")) {
            ci.cancel();
            chat(player, "§a=== Current Keybinds ===");
            for (HackModule m : HackClient.moduleManager.getAllModules()) {
                if (m.getKeybind() != -1) {
                    String key = HackModule.friendlyKeyName(m.getKeybind());
                    chat(player, "§f" + m.getName() + ": §7[" + key + "]");
                }
            }
            return;
        }

        if (lower.startsWith(".bind ")) {
            ci.cancel();
            handleBind(player, message.substring(6).trim());
        }
    }

    private void handleBind(LocalPlayer player, String args) {
        String[] split = args.split(" ");
        if (split.length < 2) {
            chat(player, "§cUsage: .bind <module> <key>");
            return;
        }

        String modName = split[0];
        String keyName = split[1].toLowerCase();

        HackModule target = HackClient.moduleManager.getAllModules().stream()
                .filter(m -> m.getName().equalsIgnoreCase(modName))
                .findFirst().orElse(null);

        if (target == null) {
            chat(player, "§cModule not found: §f" + modName);
            return;
        }

        if (keyName.equals("none") || keyName.equals("null")) {
            target.setKeybind(-1);
            chat(player, "§aRemoved keybind from §f" + target.getName());
            return;
        }

        int glfwKey = resolveKey(keyName);
        if (glfwKey == -1) {
            chat(player, "§cUnknown key: §f" + keyName);
            return;
        }

        target.setKeybind(glfwKey);
        String friendly = HackModule.friendlyKeyName(glfwKey);
        chat(player, "§aBound §f" + target.getName() + " §ato §f[" + friendly + "]");
    }

    private void chat(LocalPlayer player, String text) {
        player.sendSystemMessage(Component.literal(text));
    }

    private int resolveKey(String n) {
        switch (n) {
            case "a": return GLFW.GLFW_KEY_A; case "b": return GLFW.GLFW_KEY_B;
            case "c": return GLFW.GLFW_KEY_C; case "d": return GLFW.GLFW_KEY_D;
            case "e": return GLFW.GLFW_KEY_E; case "f": return GLFW.GLFW_KEY_F;
            case "g": return GLFW.GLFW_KEY_G; case "h": return GLFW.GLFW_KEY_H;
            case "i": return GLFW.GLFW_KEY_I; case "j": return GLFW.GLFW_KEY_J;
            case "k": return GLFW.GLFW_KEY_K; case "l": return GLFW.GLFW_KEY_L;
            case "m": return GLFW.GLFW_KEY_M; case "n": return GLFW.GLFW_KEY_N;
            case "o": return GLFW.GLFW_KEY_O; case "p": return GLFW.GLFW_KEY_P;
            case "q": return GLFW.GLFW_KEY_Q; case "r": return GLFW.GLFW_KEY_R;
            case "s": return GLFW.GLFW_KEY_S; case "t": return GLFW.GLFW_KEY_T;
            case "u": return GLFW.GLFW_KEY_U; case "v": return GLFW.GLFW_KEY_V;
            case "w": return GLFW.GLFW_KEY_W; case "x": return GLFW.GLFW_KEY_X;
            case "y": return GLFW.GLFW_KEY_Y; case "z": return GLFW.GLFW_KEY_Z;
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
            case "space": return GLFW.GLFW_KEY_SPACE;
            case "enter": return GLFW.GLFW_KEY_ENTER;
            case "escape": return GLFW.GLFW_KEY_ESCAPE;
            case "up": return GLFW.GLFW_KEY_UP;
            case "down": return GLFW.GLFW_KEY_DOWN;
            case "left": return GLFW.GLFW_KEY_LEFT;
            case "right": return GLFW.GLFW_KEY_RIGHT;
            default: return -1;
        }
    }
}
