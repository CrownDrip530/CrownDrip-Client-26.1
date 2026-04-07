package com.hack.mixins;

import com.hack.HackClient;
import com.hack.gui.HudOverlay;
import com.hack.modules.visual.ArmorESP;
import com.hack.modules.visual.NameTags;
import com.hack.modules.visual.PlayerESP;
import com.hack.modules.combat.HitboxExtend;
import com.hack.modules.visual.Tracers;
import com.hack.modules.visual.InvSee;
import com.hack.modules.visual.TargetESP;
import com.hack.modules.visual.AntiBlind;
import com.hack.modules.visual.StorageESP;
import com.hack.modules.visual.CaveMapper;
import com.hack.modules.visual.Xray;
import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.DeltaTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * InGameHudMixin — renders all visual hacks via GuiGraphics.
 * Called every frame with a valid GuiGraphics from Minecraft.
 */
@Mixin(net.minecraft.client.gui.Gui.class)
public class InGameHudMixin {

    private static final HudOverlay hudOverlay = new HudOverlay();

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("RETURN"))
    private void onRender(GuiGraphics ctx, DeltaTracker tickCounter, CallbackInfo ci) {
        if (HackClient.moduleManager == null) return;

        // HUD overlay (watermark + arraylist)
        hudOverlay.render(ctx);

        // Visual modules
        PlayerESP esp = HackClient.moduleManager.get(PlayerESP.class);
        if (esp != null) esp.renderHud(ctx);

        Tracers tracers = HackClient.moduleManager.get(Tracers.class);
        if (tracers != null) tracers.renderHud(ctx);

        NameTags nameTags = HackClient.moduleManager.get(NameTags.class);
        if (nameTags != null) nameTags.renderHud(ctx);

        ArmorESP armorESP = HackClient.moduleManager.get(ArmorESP.class);
        if (armorESP != null) armorESP.renderHud(ctx);

        HitboxExtend hitbox = HackClient.moduleManager.get(HitboxExtend.class);
        if (hitbox != null) hitbox.renderHud(ctx);

        Xray xray = HackClient.moduleManager.get(Xray.class);
        if (xray != null) xray.renderHud(ctx);

        StorageESP storage = HackClient.moduleManager.get(StorageESP.class);
        if (storage != null) storage.renderHud(ctx);

        CaveMapper cave = HackClient.moduleManager.get(CaveMapper.class);
        if (cave != null) cave.renderHud(ctx);

        TargetESP targetEsp = HackClient.moduleManager.get(TargetESP.class);
        if (targetEsp != null) targetEsp.renderHud(ctx);

        InvSee invSee = HackClient.moduleManager.get(InvSee.class);
        if (invSee != null) invSee.renderHud(ctx);
    }
}
