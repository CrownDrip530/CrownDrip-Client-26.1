package com.hack.modules.combat;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;

/**
 * AntiPotion — removes negative status effects every tick.
 */
public class AntiPotion extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    private static final String[] NEGATIVE_EFFECTS = {
        "slowness", "weakness", "blindness", "nausea", "poison",
        "wither", "mining_fatigue", "hunger", "levitation",
        "darkness", "bad_omen", "unluck"
    };

    public AntiPotion() {
        super("AntiPotion", "Combat");
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        LocalPlayer p = mc.player;
        if (p == null) return;

        for (String effectId : NEGATIVE_EFFECTS) {
            try {
                // Use getEntry(Identifier) which exists in 1.21.11
                Holder<MobEffect> entry = BuiltInRegistries.MOB_EFFECT
                    .get(net.minecraft.resources.ResourceLocation.parse("minecraft:" + effectId))
                    .orElse(null);

                if (entry != null && p.hasMobEffect(entry)) {
                    p.removeMobEffect(entry);
                }
            } catch (Exception ignored) {}
        }
    }
}
