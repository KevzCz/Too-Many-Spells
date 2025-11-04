package net.pixeldreamstudios.tms;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.pixeldreamstudios.tms.network.payload.SummonSyncPayload;
import net.pixeldreamstudios.tms.util.SummonTracker;

@Environment(EnvType.CLIENT)
public class TooManySpellsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SummonSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (payload.isRegistering()) {
                    SummonTracker.clientRegisterSummon(payload.entityUuid());
                } else {
                    SummonTracker.clientUnregisterSummon(payload.entityUuid());
                }
            });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            SummonTracker.clientClearAll();
        });

        TooManySpells.LOGGER.info("Too Many Spells client initialized!");
    }
}