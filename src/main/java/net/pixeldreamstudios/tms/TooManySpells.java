package net.pixeldreamstudios.tms;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.pixeldreamstudios.tms.config.TMSSoulsweaponsConfig;
import net.pixeldreamstudios.tms.registry.TMSSpells;
import net.pixeldreamstudios.tms.util.ModCompatibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TooManySpells implements ModInitializer {
	public static final String MOD_ID = "too-many-spells";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Too Many Spells...");

		TMSSpells.register();
		if (ModCompatibility.isSoulslikeWeaponryLoaded()) {
			TMSSoulsweaponsConfig.load(FabricLoader.getInstance().getConfigDir());
		}
		LOGGER.info("Too Many Spells initialized!");
	}

}