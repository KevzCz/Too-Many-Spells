package net.pixeldreamstudios.tms.event;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.pixeldreamstudios.tms.util.SummonTracker;

public class PlayerEventHandler {

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            SummonTracker.cleanupPlayer(handler.player.getUuid());
        });
    }
}