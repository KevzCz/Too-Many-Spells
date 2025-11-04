package net.pixeldreamstudios.tms;

import net.fabricmc.api.ModInitializer;
import net.pixeldreamstudios.tms.registry.TMSSpells;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TooManySpells implements ModInitializer {
	public static final String MOD_ID = "too-many-spells";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Too Many Spells...");

		TMSSpells.register();

		LOGGER.info("Too Many Spells initialized!");
	}
}