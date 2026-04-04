package com.hack.modules.movement;

import com.hack.modules.HackModule;

/**
 * Step — walk up blocks without jumping.
 *
 * HOW IT WORKS:
 * StepMixin overrides getStepHeight() for the local player to return
 * the configured height. Vanilla movement uses this value to decide
 * how high of a step to auto-climb when walking into blocks.
 *
 * Default is 0.6 (just under 1 block).
 * At 1.0 you can walk up full 1-block steps without jumping.
 * At 2.0 you can walk up 2-block walls instantly.
 *
 * CATEGORY: Movement
 */
public class Step extends HackModule {

    public final Setting heightSetting = new Setting("Height", 1.0f, 0.6f, 3.0f);

    public Step() {
        super("Step", "Movement");
        settings.add(heightSetting);
        alwaysShowSettings = true;
    }
}
