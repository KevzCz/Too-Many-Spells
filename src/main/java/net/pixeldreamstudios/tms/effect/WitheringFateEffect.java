package net.pixeldreamstudios.tms.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class WitheringFateEffect extends StatusEffect {

    private static final Identifier DAMAGE_TAKEN_ID = Identifier.of("spell_engine", "damage_taken");

    public WitheringFateEffect() {
        super(StatusEffectCategory.HARMFUL, 0x8B0000);

        // +10% damage taken (base), +2.5% per amplifier
        addAttributeModifier(
                Registries.ATTRIBUTE.getEntry(DAMAGE_TAKEN_ID).orElseThrow(),
                Identifier.of("too-many-spells", "withering_fate_damage"),
                0.10,
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        // -10% movement speed (base), -5% per amplifier
        addAttributeModifier(
                EntityAttributes.GENERIC_MOVEMENT_SPEED,
                Identifier.of("too-many-spells", "withering_fate_speed"),
                -0.10,
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