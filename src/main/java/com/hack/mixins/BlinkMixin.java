package com.hack.mixins;

import com.hack.HackClient;
import com.hack.modules.utility.Blink;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BlinkMixin — intercepts sendPacket to buffer packets when Blink is active.
 */
@Mixin(ClientPacketListener.class)
public class BlinkMixin {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V",
            at = @At("HEAD"), cancellable = true)
    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        if (HackClient.moduleManager == null) return;
        Blink blink = HackClient.moduleManager.get(Blink.class);
        if (blink != null && blink.interceptPacket(packet)) {
            ci.cancel(); // suppress packet, buffer it
        }
    }
}
