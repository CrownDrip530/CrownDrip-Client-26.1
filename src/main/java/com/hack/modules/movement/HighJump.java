package com.hack.modules.movement;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * HighJump — jump much higher than normal.
 */
public class HighJump extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    public final Setting heightSetting = new Setting("Height", 2.0f, 1.0f, 10.0f);

    public HighJump() {
        super("HighJump", "Movement");
        settings.add(heightSetting);
        alwaysShowSettings = true;
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;
        // When jumping, boost Y velocity
        if (p.getVelocity().y > 0.1 && p.getVelocity().y < 0.5) {
            p.setVelocity(p.getVelocity().x,
                p.getVelocity().y * heightSetting.value,
                p.getVelocity().z);
        }
    }
}
