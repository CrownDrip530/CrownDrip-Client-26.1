package com.hack.modules.visual;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;

/**
 * Fullbright - Makes everything fully lit.
 *
 * 1.21.1: gamma field is private. Use options.gamma which returns
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
        originalGamma = (Double) mc.options.gamma().get();
        mc.options.gamma().set(10.0);
    }

    @Override
    public void onDisable() {
        mc.options.gamma().set(originalGamma);
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        if ((Double) mc.options.gamma().get() < 9.0) {
            mc.options.gamma().set(10.0);
        }
    }
}
