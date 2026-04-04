package com.hack.mixins;

import com.hack.HackClient;
import com.hack.modules.combat.ReachHack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * NetworkMixin - Extends attack reach by injecting into attackEntity.
 *
 * 1.21.11: Instead of hooking doAttack() (which is private and only fires
 * when vanilla already decided to attack), we hook attackEntity() in
 * MultiPlayerGameMode which is always called when an attack happens.
 * ReachHack.onTick() handles extending the reach by calling attackEntity
 * directly when the attack key is pressed and a far target is in range.
 */
@Mixin(MultiPlayerGameMode.class)
public class NetworkMixin {

    // This mixin is intentionally minimal - Reach is handled in ReachHack.onTick()
    // via direct interactionManager.attackEntity() calls when attack key is pressed

}
