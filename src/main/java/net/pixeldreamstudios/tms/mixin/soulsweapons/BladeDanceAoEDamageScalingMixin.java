package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.soulsweaponry.items.BladeDanceItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(value = BladeDanceItem.class)
public abstract class BladeDanceAoEDamageScalingMixin {

    @Unique
    private static float tmscompat$factorFromSource(DamageSource src) {
        Entity atk = src.getAttacker();
        if (!(atk instanceof LivingEntity user)) return 1.0F;

        float adBaseline = ConfigHelper.getBaselineValue("blade_dance.attack_damage_baseline", 8.0F);
        float asBaseline = ConfigHelper.getBaselineValue("blade_dance.attack_speed_baseline", 1.3F);
        float adWeight = ConfigHelper.getFloatValue("blade_dance.attack_damage_weight", 0.75F);
        float asWeight = ConfigHelper.getFloatValue("blade_dance.attack_speed_weight", 0.75F);

        double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        double as = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_SPEED);

        float adPart = adBaseline > 0.0F ? (float)(ad / adBaseline) : 1.0F;
        float asPart = asBaseline > 0.0F ? (float)(as / asBaseline) : 1.0F;

        return 1.0F + adWeight * (adPart - 1.0F) + asWeight * (asPart - 1.0F);
    }

    @Redirect(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            ),
            require = 0
    )
    private boolean tmscompat$scaleAoESweep(LivingEntity target, DamageSource source, float amount) {
        return target.damage(source, amount * tmscompat$factorFromSource(source));
    }
}