package net.pixeldreamstudios.tms.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class MarkedForDeathEffect extends StatusEffect {

    private static final Identifier DAMAGE_TAKEN_ID = Identifier.of("spell_engine", "damage_taken");

    public MarkedForDeathEffect() {
        super(StatusEffectCategory.HARMFUL, 0x8B0000);

        addAttributeModifier(
                Registries.ATTRIBUTE.getEntry(DAMAGE_TAKEN_ID).orElseThrow(),
                Identifier.of("too-many-spells", "marked_for_death_damage"),
                0.05,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );
    }

    @Override
    public boolean applyUpdateEffect(LivingEntity entity, int amplifier) {
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    public static int calculateAmplifier(double soulPower) {
        int amplifier = (int) (soulPower / 40.0);
        return Math.min(10, amplifier);
    }
}