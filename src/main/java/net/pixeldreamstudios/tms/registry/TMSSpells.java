package net.pixeldreamstudios.tms.registry;

import net.minecraft.util.Identifier;
import net.pixeldreamstudios.tms.TooManySpells;
import net.pixeldreamstudios.tms.spell.handler.FlamePillarProcHandler;
import net.pixeldreamstudios.tms.spell.handler.FreyrSwordDeliveryHandler;
import net.pixeldreamstudios.tms.spell.handler.SummonEvokerFangsHandler;
import net.pixeldreamstudios.tms.spell.handler.TrueFreyrSwordDeliveryHandler;
import net.pixeldreamstudios.tms.util.ModCompatibility;
import net.spell_engine.api.spell.event.SpellHandlers;

public class TMSSpells {

    public static final Identifier SUMMON_FREYR_SWORD = Identifier.of(TooManySpells.MOD_ID, "summon_freyr_sword");
    public static final Identifier FLAME_PILLAR_PROC = Identifier.of(TooManySpells.MOD_ID, "flame_pillar_proc");
    public static final Identifier SUMMON_TRUE_FREYR_SWORD = Identifier.of(TooManySpells.MOD_ID, "summon_true_freyr_sword");
    public static final Identifier SUMMON_EVOKER_FANGS = Identifier.of(TooManySpells.MOD_ID, "summon_evoker_fangs");

    public static void register() {
        if (ModCompatibility.isSoulslikeWeaponryLoaded()) {
            registerSoulslikeSpellsHandlers();
        }
        registerSummonSpells();
    }

    private static void registerSoulslikeSpellsHandlers() {
        SpellHandlers.registerCustomDelivery(
                SUMMON_FREYR_SWORD,
                new FreyrSwordDeliveryHandler()
        );

        SpellHandlers.registerCustomDelivery(
                FLAME_PILLAR_PROC,
                new FlamePillarProcHandler()
        );

        SpellHandlers.registerCustomDelivery(
                SUMMON_TRUE_FREYR_SWORD,
                new TrueFreyrSwordDeliveryHandler()
        );
    }
    private static void registerSummonSpells() {
        SpellHandlers.registerCustomDelivery(
                SUMMON_EVOKER_FANGS,
                new SummonEvokerFangsHandler()
        );

    }
}