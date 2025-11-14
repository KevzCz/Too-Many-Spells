package net.pixeldreamstudios.tms.mixin.soulsweapons.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.pixeldreamstudios.tms.util.soulsweapons.ExtendedFreyrSwordData;
import net.pixeldreamstudios.tms.util.soulsweapons.ExtendedTrueFreyrSwordData;
import net.soulsweaponry.entity.ai.goal.FreyrSwordGoal;
import net.soulsweaponry.entity.mobs.FreyrSwordEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = FreyrSwordGoal.class)
public abstract class FreyrSwordGoalMixin {
    @Shadow(remap = false) private FreyrSwordEntity entity;

    @Inject(method = "<init>", at = @At("RETURN"), require = 0)
    private void tmscompat$increaseFollowRange(FreyrSwordEntity entity, CallbackInfo ci) {
        if (entity != null && SummonTracker.isSpellSummon(entity.getUuid())) {
            var summonData = SummonTracker.getSummonData(entity.getUuid());
            if (summonData != null) {
                double baseFollowRange = 32.0;

                if (summonData.summonType.equals(ExtendedTrueFreyrSwordData.SUMMON_TYPE)) {
                    var scaleAttr = entity.getAttributeInstance(EntityAttributes.GENERIC_SCALE);
                    if (scaleAttr != null) {
                        double scale = scaleAttr.getValue();
                        baseFollowRange = 32.0 * scale;
                    }
                }

                var followRangeAttr = entity.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE);
                if (followRangeAttr != null) {
                    followRangeAttr.setBaseValue(baseFollowRange);
                }
            }
        }
    }

    @Inject(method = "getAttackDamage", at = @At("RETURN"), cancellable = true, require = 0)
    private void tmscompat$addEntityAttackAttribute(LivingEntity target, CallbackInfoReturnable<Float> cir) {
        if (this.entity == null) {
            return;
        }

        float originalDamage = cir.getReturnValueF();

        double attackDamageAttr = this.entity.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

        double soulSpellPowerAttr = 0.0D;
        var soulAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.SOUL);
        if (soulAttr != null && this.entity.getAttributes().hasAttribute(soulAttr)) {
            soulSpellPowerAttr = this.entity.getAttributeValue(soulAttr);
        }

        float attackDamageBaseline = ConfigHelper.getBaselineValue("freyr_sword.attack_damage_baseline", 7.0F);
        float soulBaseline = ConfigHelper.getBaselineValue("freyr_sword.soul_baseline", 20.0F);
        float attackDamageWeight = ConfigHelper.getFloatValue("freyr_sword.attack_damage_weight", 0.75F);
        float soulWeight = ConfigHelper.getFloatValue("freyr_sword.soul_weight", 2.5F);

        float excessAttackDamage = (float) Math.max(0.0D, attackDamageAttr - attackDamageBaseline);

        float excessSoulPower = (float) Math.max(0.0D, soulSpellPowerAttr - soulBaseline);

        float soulMultiplier;
        if (ExtendedFreyrSwordData.isSpellSummon(entity.getUuid())) {
            soulMultiplier = 1.0F;
        } else {
            soulMultiplier = 2.5F + (soulWeight * excessSoulPower / soulBaseline);
        }

        float attackDamageBonus = (excessAttackDamage * attackDamageWeight) * soulMultiplier;
        float soulDamageBonus = (attackDamageBaseline * (soulMultiplier - 1.0F));

        cir.setReturnValue(originalDamage + attackDamageBonus + soulDamageBonus);
    }
}