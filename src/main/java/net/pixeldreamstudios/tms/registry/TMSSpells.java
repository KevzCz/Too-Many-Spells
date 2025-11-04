package net.pixeldreamstudios.tms.registry;

import net.minecraft.util.Identifier;
import net.pixeldreamstudios.tms.TooManySpells;
import net.pixeldreamstudios.tms.spell.handler.FreyrSwordDeliveryHandler;
import net.pixeldreamstudios.tms.util.ModCompatibility;
import net.spell_engine.api.spell.event.SpellHandlers;

public class TMSSpells {

    public static void register() {
        if (ModCompatibility.isSoulslikeWeaponryLoaded()) {
            registerHandlers();
        }
    }

    private static void registerHandlers() {
        SpellHandlers.registerCustomDelivery(
                Identifier.of(TooManySpells.MOD_ID, "summon_freyr_sword"),
                new FreyrSwordDeliveryHandler()
        );
    }
}