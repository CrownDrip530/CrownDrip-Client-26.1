package com.hack.mixins;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * MinecraftClientAccessor — exposes private fields needed by various modules.
 */
@Mixin(Minecraft.class)
public interface MinecraftClientAccessor {

    @Accessor("itemUseCooldown")
    void setItemUseCooldown(int cooldown);

    @Accessor("itemUseCooldown")
    int getItemUseCooldown();
}
