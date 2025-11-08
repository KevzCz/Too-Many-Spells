package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.entity.projectile.LeviathanAxeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(LeviathanAxeEntity.class)
public abstract class LeviathanAxeEntityMixin {
    @Unique private static final ThreadLocal<Float> tmscompat$damageFactor = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(method = "getDamage", at = @At("HEAD"))
    private void tmscompat$cacheFactor(Entity target, CallbackInfoReturnable<Float> cir) {
        float ad = 0F, frost = 0F;

        float adBaseline = ConfigHelper.getBaselineValue("leviathan_axe.attack_damage_baseline", 10.0F);
        float frostBaseline = ConfigHelper.getBaselineValue("leviathan_axe.frost_baseline", 20.0F);
        float adWeight = ConfigHelper.getFloatValue("leviathan_axe.attack_damage_weight", 0.5F);
        float frostWeight = ConfigHelper.getFloatValue("leviathan_axe.frost_weight", 0.5F);

        LeviathanAxeEntity self = (LeviathanAxeEntity)(Object)this;
        Entity owner = self.getOwner();
        if (owner instanceof LivingEntity living) {
            ad = (float) living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

            RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.FROST);
            if (entry != null) {
                frost = (float) living.getAttributeValue(entry);
            }
        }

        float adPart = adBaseline > 0.0F ? ad / adBaseline : 1.0F;
        float frostPart = frostBaseline > 0.0F ? frost / frostBaseline : 0.0F;
        float factor = adWeight * adPart + frostWeight * frostPart;
        if (factor < 0F) factor = 0F;
        tmscompat$damageFactor.set(factor);
    }

    @Inject(method = "getDamage", at = @At("RETURN"), cancellable = true)
    private void tmscompat$scaleThrownDamage(Entity target, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(cir.getReturnValueF() * tmscompat$damageFactor.get());
        tmscompat$damageFactor.remove();
    }

    @Redirect(
            method = "collide",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;addStatusEffect(Lnet/minecraft/entity/effect/StatusEffectInstance;)Z")
    )
    private boolean tmscompat$addStatusEffectWithFrostBonus(LivingEntity target, StatusEffectInstance original) {
        LeviathanAxeEntity self = (LeviathanAxeEntity)(Object)this;
        Entity owner = self.getOwner();

        int bonus = 0;
        if (owner instanceof LivingEntity living) {
            RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.FROST);
            if (entry != null) {
                float frostPerAmp = ConfigHelper.getBaselineValue("leviathan_axe.frost_per_amplifier", 10.0F);
                bonus = (int)Math.floor(living.getAttributeValue(entry) / frostPerAmp);
            }
        }

        RegistryEntry<StatusEffect> type = original.getEffectType();
        int duration = original.getDuration();
        int amp = Math.max(0, original.getAmplifier() + bonus);
        return target.addStatusEffect(new StatusEffectInstance(type, duration, amp));
    }
}