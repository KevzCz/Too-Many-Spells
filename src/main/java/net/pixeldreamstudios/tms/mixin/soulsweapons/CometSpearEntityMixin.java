package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.hit.EntityHitResult;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.soulsweaponry.entity.projectile.CometSpearEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(value = CometSpearEntity.class)
public abstract class CometSpearEntityMixin {

    @Redirect(
            method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            ),
            require = 0
    )
    private boolean tmscompat$scaleCometDamage(Entity target, DamageSource source, float amount, EntityHitResult hit) {
        CometSpearEntity self = (CometSpearEntity)(Object)this;

        float factor = 1.0F;
        Entity owner = self.getOwner();
        float baseline = ConfigHelper.getBaselineValue("comet_spear.attack_damage_baseline", 8.0F);

        if (owner instanceof LivingEntity living && baseline > 0.0F) {
            double ad = living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (ad > 0.0) factor = (float)(ad / baseline);
        }

        return target.damage(source, amount * factor);
    }
}