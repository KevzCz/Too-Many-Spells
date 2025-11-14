package net.pixeldreamstudios.tms.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class FatedVictoryEffect extends StatusEffect {

    private static final Identifier CRIT_CHANCE_ID = Identifier.of("critical_strike", "chance");
    private static final Identifier SUMMON_CRIT_CHANCE_ID = Identifier.of("summonerlib", "summon_crit_chance");

    public FatedVictoryEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x4FC3F7);

        // +5% critical hit rate (base), +2.5% per amplifier
        addAttributeModifier(
                Registries.ATTRIBUTE.getEntry(CRIT_CHANCE_ID).orElseThrow(),
                Identifier.of("too-many-spells", "fated_victory_crit"),
                0.05,
                EntityAttributeModifier.Operation.ADD_VALUE
        );

        // +5% summon crit hit rate (base), +2.5% per amplifier
        addAttributeModifier(
                Registries.ATTRIBUTE.getEntry(SUMMON_CRIT_CHANCE_ID).orElseThrow(),
                Identifier.of("too-many-spells", "fated_victory_summon_crit"),
                0.05,
                EntityAttributeModifier.Operation.ADD_VALUE
        );

        // +10% movement speed (base), +5% per amplifier
        addAttributeModifier(
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                Identifier.of("too-many-spells", "fated_victory_speed"),
                0.10,
                EntityAttributeModifier.    Operation.ADD_MULTIPLIED_BASE
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

    @Override
    public void onApplied(LivingEntity entity, int amplifier) {
        super.onApplied(entity, amplifier);
    }

    @Override
    public void onRemoved(AttributeContainer attributes) {
        super.onRemoved(attributes);
    }
    public static int getAmplifierForMergeCount(int mergeCount) {
        return mergeCount / 2;
    }
}