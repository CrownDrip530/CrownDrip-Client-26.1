package com.hack.modules.visual;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

/**
 * NameTags - Shows player names and distance above their heads.
 * Uses screen-space rendering via InGameHudMixin.
 */
public class NameTags extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    public NameTags() {
        super("NameTags", "Visual");
    }

    public void renderHud(GuiGraphics ctx) {
        if (!isEnabled() || mc.level == null || mc.player == null) return;

        for (Player player : mc.level.players()) {
            if (player == mc.player) continue;

            Vec3 pos = new Vec3(player.getX(), player.getY() + player.getBbHeight() + 0.3, player.getZ());
            int[] screen = ArmorESP.staticWorldToScreen(pos, mc);
            if (screen == null) continue;

            double dist = mc.player.distanceTo(player);
            String name = player.getName().getString();
            // Use Component.literal for proper color code support
            net.minecraft.network.chat.Text label = Component.literal(name)
                .append(Component.literal(" [" + (int)dist + "m]")
                    .styled(s -> s.withColor(0xAAAAAA)));

            int textW = mc.textRenderer.getWidth(label);
            int sx = screen[0] - textW / 2;
            int sy = screen[1];

            // Background
            ctx.fill(sx - 2, sy - 1, sx + textW + 2, sy + mc.textRenderer.fontHeight + 1, 0x88000000);
            // Text
            ctx.drawText(mc.textRenderer, label, sx, sy, 0xFFFFFFFF, true);
        }
    }
}
