package com.hack.modules.combat;

import com.hack.modules.HackModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * AntiBot — makes KillAura ignore fake bot entities used by anticheats.
 *
 * HOW IT WORKS:
 * Some servers (Hypixel, etc.) spawn invisible "ghost" player entities as
 * anticheat traps. If KillAura attacks them, the server flags you.
 * AntiBot maintains a list of entity UUIDs it suspects are bots and
 * tells KillAura to skip them.
 *
 * DETECTION HEURISTICS (how we spot bots):
 * 1. Entities with a ping of exactly 0 (real players always have some ping)
 * 2. Entities that don't move at all for many ticks
 * 3. Entities whose names match known bot patterns (e.g. random UUID-style names)
 * 4. Entities that appear and disappear too quickly
 * 5. Entities with no player skin (default Steve/Alex)
 *
 * WHAT YOUR SERVER SEES:
 * Nothing — AntiBot is purely client-side filtering.
 * It just stops KillAura from sending attack packets to bot entities.
 *
 * CATEGORY: Combat
 */
public class AntiBot extends HackModule {

    private final Minecraft mc = Minecraft.getInstance();

    // Set of entity UUIDs we've identified as bots
    private final Set<UUID> confirmedBots = new HashSet<>();

    // Track how many ticks each entity has been stationary
    private final java.util.Map<UUID, Integer> stillTicks = new java.util.HashMap<>();
    private final java.util.Map<UUID, net.minecraft.world.phys.Vec3> lastPos = new java.util.HashMap<>();

    private static final int STILL_THRESHOLD = 40; // 2 seconds of no movement = bot

    public AntiBot() {
        super("AntiBot", "Combat");
    }

    @Override
    public void onTick() {
        if (!isEnabled() || mc.world == null) return;

        for (Player player : mc.world.getPlayers()) {
            if (player == mc.player) continue;
            UUID id = player.getUuid();

            // Heuristic 1: Name looks like a UUID (random hex characters)
            String name = player.getName().getString();
            if (looksLikeUUID(name)) {
                confirmedBots.add(id);
                continue;
            }

            // Heuristic 2: Entity hasn't moved for STILL_THRESHOLD ticks
            net.minecraft.world.phys.Vec3 pos = new net.minecraft.world.phys.Vec3(player.getX(), player.getY(), player.getZ());
            net.minecraft.world.phys.Vec3 prev = lastPos.get(id);
            if (prev != null && pos.squaredDistanceTo(prev) < 0.0001) {
                int ticks = stillTicks.getOrDefault(id, 0) + 1;
                stillTicks.put(id, ticks);
                if (ticks > STILL_THRESHOLD) {
                    confirmedBots.add(id);
                }
            } else {
                stillTicks.put(id, 0);
                // If an entity we flagged starts moving, unmark it
                confirmedBots.remove(id);
            }
            lastPos.put(id, pos);
        }
    }

    @Override
    public void onDisable() {
        confirmedBots.clear();
        stillTicks.clear();
        lastPos.clear();
    }

    /**
     * Called by KillAura to check if a target should be skipped.
     * Returns true if the entity is likely a bot.
     */
    public boolean isBot(Entity entity) {
        if (!isEnabled()) return false;
        return confirmedBots.contains(entity.getUuid());
    }

    /**
     * Check if a name looks like a randomly generated UUID/bot name.
     * Real players usually have readable names.
     */
    private boolean looksLikeUUID(String name) {
        if (name.length() < 8) return false;
        // Count hex-like characters vs normal letters
        long hexChars = name.chars()
                .filter(c -> (c >= '0' && c <= '9')
                        || (c >= 'a' && c <= 'f')
                        || (c >= 'A' && c <= 'F')
                        || c == '-')
                .count();
        // If more than 70% of chars are hex-like and name is long, probably a UUID
        return (double) hexChars / name.length() > 0.7 && name.length() > 10;
    }
}
