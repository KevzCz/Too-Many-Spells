package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EvokerFangsEntity.class)
public abstract class EvokerFangsSoulmassScalingMixin {

    @Unique
    private static float tmscompat$factorFromOwner(EvokerFangsEntity self) {
        LivingEntity owner = self.getOwner();
        if (owner == null) return 1.0F;

        float soulBaseline = ConfigHelper.getBaselineValue("evoker_fangs.soul_baseline", 10.0F);

        RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.SOUL);

        if (entry == null || soulBaseline <= 0.0F) return 1.0F;

        double soul = owner.getAttributeValue(entry);
        float factor = (float)(soul / soulBaseline);
        return Math.max(0.0F, factor);
    }

    @Redirect(
            method = "damage(Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    ordinal = 0
            ),
            require = 0
    )
    private boolean tmscompat$scaleFangsDamageNoOwner(LivingEntity target,
                                                      net.minecraft.entity.damage.DamageSource src,
                                                      float amount) {
        EvokerFangsEntity self = (EvokerFangsEntity)(Object)this;
        return target.damage(src, amount * tmscompat$factorFromOwner(self));
    }

    @Redirect(
            method = "damage(Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    ordinal = 1
            ),
            require = 0
    )
    private boolean tmscompat$scaleFangsDamageWithOwner(LivingEntity target,
                                                        net.minecraft.entity.damage.DamageSource src,
                                                        float amount) {
        EvokerFangsEntity self = (EvokerFangsEntity)(Object)this;
        return target.damage(src, amount * tmscompat$factorFromOwner(self));
    }
}