package net.pixeldreamstudios.tms;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.pixeldreamstudios.tms.event.PlayerEventHandler;
import net.pixeldreamstudios.tms.event.SummonTickHandler;
import net.pixeldreamstudios.tms.network.payload.SummonSyncPayload;
import net.pixeldreamstudios.tms.registry.TMSSpells;

public class TooManySpells implements ModInitializer {
	public static final String MOD_ID = "too-many-spells";

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.playS2C().register(SummonSyncPayload.ID, SummonSyncPayload.CODEC);
		SummonTickHandler.register();
		PlayerEventHandler.register();
		TMSSpells.register();

	}
}