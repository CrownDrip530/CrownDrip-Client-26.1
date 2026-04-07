package com.hack;

import com.hack.modules.HackModule;
// Movement
import com.hack.modules.movement.FlyHack;
import com.hack.modules.movement.SpeedHack;
import com.hack.modules.movement.NoFall;
import com.hack.modules.movement.Jesus;
import com.hack.modules.movement.Spider;
import com.hack.modules.movement.BoatFly;
// Combat
import com.hack.modules.combat.ReachHack;
import com.hack.modules.combat.HitboxExtend;
import com.hack.modules.combat.KillAura;
import com.hack.modules.combat.AntiKnockback;
import com.hack.modules.combat.AutoClicker;
import com.hack.modules.combat.Criticals;
import com.hack.modules.combat.AntiBot;
import com.hack.modules.visual.InvSee;
import com.hack.modules.visual.TargetESP;
import com.hack.modules.visual.AntiBlind;
import com.hack.modules.utility.AntiHunger;
import com.hack.modules.movement.Phase;
import com.hack.modules.visual.StorageESP;
import com.hack.modules.visual.CaveMapper;
import com.hack.modules.visual.Xray;
import com.hack.modules.utility.ClickTP;
import com.hack.modules.utility.ChestStealer;
import com.hack.modules.utility.AutoArmor;
import com.hack.modules.utility.AutoEat;
import com.hack.modules.utility.Nuker;
import com.hack.modules.utility.FastPlace;
import com.hack.modules.utility.Blink;
import com.hack.modules.combat.Derp;
import com.hack.modules.combat.AntiAim;
import com.hack.modules.combat.AutoCrystal;
import com.hack.modules.movement.SafeWalk;
import com.hack.modules.movement.HighJump;
import com.hack.modules.movement.BunnyHop;
import com.hack.modules.movement.WaterSpeed;
import com.hack.modules.movement.LongJump;
import com.hack.modules.movement.Step;
import com.hack.modules.combat.Velocity;
import com.hack.modules.combat.AntiPotion;
import com.hack.modules.utility.AntiLagBack;
import com.hack.modules.utility.MadeInHeaven;
// Visual
import com.hack.modules.visual.PlayerESP;
import com.hack.modules.visual.Fullbright;
import com.hack.modules.visual.Tracers;
import com.hack.modules.visual.NameTags;
import com.hack.modules.visual.ArmorESP;
// Utility
import com.hack.modules.utility.NoClip;
import com.hack.modules.utility.Scaffold;
import com.hack.modules.utility.Freecam;
import com.hack.modules.utility.Teleport;
import com.hack.modules.utility.Disabler;
import com.hack.modules.utility.AutoTotem;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {

    private final List<HackModule> modules = new ArrayList<>();

    public void init() {
        // Movement
        modules.add(new FlyHack());
        modules.add(new Phase());
        modules.add(new SafeWalk());
        modules.add(new HighJump());
        modules.add(new BunnyHop());
        modules.add(new WaterSpeed());
        modules.add(new LongJump());
        modules.add(new Step());
        modules.add(new SpeedHack());
        modules.add(new NoFall());
        modules.add(new Jesus());
        modules.add(new Spider());
        modules.add(new BoatFly());
        // Combat
        modules.add(new ReachHack());
        modules.add(new HitboxExtend());
        modules.add(new KillAura());
        modules.add(new AntiKnockback());
        modules.add(new AutoClicker());
        modules.add(new Criticals());
        modules.add(new AntiBot());
        modules.add(new Derp());
        modules.add(new AntiAim());
        modules.add(new AutoCrystal());
        modules.add(new Velocity());
        modules.add(new AntiPotion());
        // Visual
        modules.add(new PlayerESP());
        modules.add(new InvSee());
        modules.add(new TargetESP());
        modules.add(new AntiBlind());
        modules.add(new StorageESP());
        modules.add(new CaveMapper());
        modules.add(new Xray());
        modules.add(new Fullbright());
        modules.add(new Tracers());
        modules.add(new NameTags());
        modules.add(new ArmorESP());
        // Utility
        modules.add(new NoClip());
        modules.add(new Scaffold());
        modules.add(new Freecam());
        modules.add(new Teleport());
        modules.add(new Disabler());
        modules.add(new AutoTotem());
        modules.add(new AntiLagBack());
        modules.add(new AntiHunger());
        modules.add(new ClickTP());
        modules.add(new ChestStealer());
        modules.add(new AutoArmor());
        modules.add(new AutoEat());
        modules.add(new Nuker());
        modules.add(new FastPlace());
        modules.add(new Blink());
        modules.add(new MadeInHeaven());
    }

    @SuppressWarnings("unchecked")
    public static <T extends HackModule> T get(Class<T> clazz) {
        if (HackClient.moduleManager == null) return null;
        return (T) HackClient.moduleManager.modules.stream()
                .filter(m -> m.getClass() == clazz)
                .findFirst().orElse(null);
    }

    public static List<HackModule> getByCategory(String category) {
        return HackClient.moduleManager.modules.stream()
                .filter(m -> m.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    public static List<HackModule> getAllModules() {
        return HackClient.moduleManager.modules;
    }

    public static List<HackModule> getEnabledModules() {
        return HackClient.moduleManager.modules.stream()
                .filter(HackModule::isEnabled)
                .collect(Collectors.toList());
    }

    public static List<HackModule> search(String query, String category) {
        return HackClient.moduleManager.modules.stream()
                .filter(m -> category == null || m.getCategory().equals(category))
                .filter(m -> query == null || query.isBlank()
                        || m.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }
}
