package com.hack.modules.visual;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

/**
 * AntiBlind — removes visual blindness and darkness effects.
 *
 * Removes: blindness, darkness, nausea client-side every tick.
 * Also keeps gamma high to counteract darkness effect's light reduction.
 */
public class AntiBlind extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    private static final String[] VISUAL_EFFECTS = {
        "blindness", "darkness", "nausea"
    };

    public AntiBlind() {
        super("AntiBlind", "Visual");
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;

        for (String effectId : VISUAL_EFFECTS) {
            try {
                Holder<MobEffect> entry = Registries.STATUS_EFFECT
                    .getOptional(ResourceLocation.parse("minecraft", effectId))
                    .orElse(null);
                if (entry != null && p.hasMobEffect(entry)) {
                    p.removeMobEffect(entry);
                }
            } catch (Exception ignored) {}
        }

        // Also boost gamma to fight darkness lighting reduction
        try {
            double current = (Double) mc.options.getGamma().getValue();
            if (current < 5.0) mc.options.getGamma().setValue(10.0);
        } catch (Exception ignored) {}
    }
}
