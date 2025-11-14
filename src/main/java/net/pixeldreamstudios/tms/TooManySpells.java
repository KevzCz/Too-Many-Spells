package net.pixeldreamstudios.tms;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.pixeldreamstudios.tms.config.TMSSoulsweaponsConfig;
import net.pixeldreamstudios.tms.registry.TMSSpells;
import net.pixeldreamstudios.tms.registry.TMSStatusEffects;
import net.pixeldreamstudios.tms.util.ModCompatibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TooManySpells implements ModInitializer {
	public static final String MOD_ID = "too-many-spells";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final boolean SOULSWEAPONRY_LOADED = ModCompatibility.isSoulslikeWeaponryLoaded();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Too Many Spells...");

		TMSStatusEffects.initialize();

		if (SOULSWEAPONRY_LOADED) {
			LOGGER.info("Marium's Soulslike Weaponry detected, loading integration...");
			TMSSoulsweaponsConfig.load(FabricLoader.getInstance().getConfigDir());
		} else {
			LOGGER.info("Marium's Soulslike Weaponry not found, skipping integration.");
		}

		TMSSpells.register();

		LOGGER.info("Too Many Spells initialized!");
	}
}