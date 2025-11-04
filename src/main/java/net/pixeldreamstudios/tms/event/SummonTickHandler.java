package net.pixeldreamstudios.tms.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.pixeldreamstudios.tms.util.SummonTracker;

public class SummonTickHandler {

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(SummonTracker::tick);
    }
}