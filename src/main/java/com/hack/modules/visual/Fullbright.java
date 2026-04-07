package com.hack.modules.visual;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;

/**
 * Fullbright - Makes everything fully lit.
 *
 * 1.21.11: gamma field is private. Use getGamma() which returns
 * SimpleOption<Double>. getValue()/setValue() work on the returned option.
 */
public class Fullbright extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    private double originalGamma = 1.0;

    public Fullbright() {
        super("Fullbright", "Visual");
    }

    @Override
    public void onEnable() {
        originalGamma = (Double) mc.options.getGamma().getValue();
        mc.options.getGamma().setValue(10.0);
    }

    @Override
    public void onDisable() {
        mc.options.getGamma().setValue(originalGamma);
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        if ((Double) mc.options.getGamma().getValue() < 9.0) {
            mc.options.getGamma().setValue(10.0);
        }
    }
}
