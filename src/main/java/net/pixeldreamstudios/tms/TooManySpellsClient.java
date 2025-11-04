package net.pixeldreamstudios.tms;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TooManySpellsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TooManySpells.LOGGER.info("Too Many Spells client initialized!");
    }
}