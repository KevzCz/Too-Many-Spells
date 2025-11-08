package net.pixeldreamstudios.tms.registry;

import net.minecraft.util.Identifier;
import net.pixeldreamstudios.tms.TooManySpells;
import net.pixeldreamstudios.tms.spell.handler.FlamePillarProcHandler;
import net.pixeldreamstudios.tms.spell.handler.FreyrSwordDeliveryHandler;
import net.pixeldreamstudios.tms.util.ModCompatibility;
import net.spell_engine.api.spell.event.SpellHandlers;

public class TMSSpells {

    public static final Identifier SUMMON_FREYR_SWORD = Identifier.of(TooManySpells.MOD_ID, "summon_freyr_sword");
    public static final Identifier FLAME_PILLAR_PROC = Identifier.of(TooManySpells.MOD_ID, "flame_pillar_proc");

    public static void register() {
        if (ModCompatibility.isSoulslikeWeaponryLoaded()) {
            registerHandlers();
        }
    }

    private static void registerHandlers() {
        SpellHandlers.registerCustomDelivery(
                SUMMON_FREYR_SWORD,
                new FreyrSwordDeliveryHandler()
        );

        SpellHandlers.registerCustomDelivery(
                FLAME_PILLAR_PROC,
                new FlamePillarProcHandler()
        );
    }
}