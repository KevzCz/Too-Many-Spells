package net.pixeldreamstudios.tms.registry;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.pixeldreamstudios.tms.TooManySpells;
import net.pixeldreamstudios.tms.effect.FatedVictoryEffect;
import net.pixeldreamstudios.tms.effect.MarkedForDeathEffect;
import net.pixeldreamstudios.tms.effect.WitheringFateEffect;

public class TMSStatusEffects {

    public static final RegistryEntry<StatusEffect> FATED_VICTORY = register(
            "fated_victory",
            new FatedVictoryEffect()
    );

    public static final RegistryEntry<StatusEffect> WITHERING_FATE = register(
            "withering_fate",
            new WitheringFateEffect()
    );

    public static final RegistryEntry<StatusEffect> MARKED_FOR_DEATH = register(
            "marked_for_death",
            new MarkedForDeathEffect()
    );

    private static RegistryEntry<StatusEffect> register(String name, StatusEffect effect) {
        return Registry.registerReference(
                Registries.STATUS_EFFECT,
                Identifier.of(TooManySpells.MOD_ID, name),
                effect
        );
    }

    public static void initialize() {
    }
}