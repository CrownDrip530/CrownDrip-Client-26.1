package com.hack.modules.utility;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Blink — holds all outgoing packets, releases them all at once on toggle.
 *
 * While active: you move freely client-side but server sees you frozen.
 * When disabled: all buffered packets flood the server simultaneously.
 * From server perspective: you teleport instantly and deal all attacks at once.
 *
 * Bind to a key for best effect. Press to start buffering, press again to release.
 */
public class Blink extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();
    private final List<Packet<?>> bufferedPackets = new ArrayList<>();

    public Blink() {
        super("Blink", "Utility");
    }

    @Override
    public void onEnable() {
        bufferedPackets.clear();
    }

    @Override
    public void onDisable() {
        if (mc.player == null || mc.player.networkHandler == null) {
            bufferedPackets.clear();
            return;
        }
        // Flush all buffered packets at once
        for (Packet<?> packet : bufferedPackets) {
            mc.player.networkHandler.sendPacket(packet);
        }
        bufferedPackets.clear();
    }

    /**
     * Called by BlinkMixin to intercept outgoing packets.
     * Returns true if the packet should be suppressed (buffered instead of sent).
     */
    private static final int MAX_PACKETS = 1000; // cap to prevent OOM

    public boolean interceptPacket(Packet<?> packet) {
        if (!isEnabled()) return false;
        if (bufferedPackets.size() >= MAX_PACKETS) {
            // Auto-disable if too many packets buffered to prevent crash
            toggle();
            return false;
        }
        bufferedPackets.add(packet);
        return true;
    }

    public int getBufferedCount() { return bufferedPackets.size(); }
}
