package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.soulsweaponry.entity.projectile.DraupnirSpearEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(value = DraupnirSpearEntity.class)
public abstract class DraupnirSpearEntityMixin {

    @Redirect(
            method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            ),
            require = 0
    )
    private boolean tmscompat$scaleProjectileDamage(Entity target, DamageSource source, float amount, EntityHitResult hit) {
        DraupnirSpearEntity self = (DraupnirSpearEntity)(Object)this;
        float factor = 1.0F;

        Entity owner = self.getOwner();
        if (owner instanceof LivingEntity living) {
            float adBaseline = ConfigHelper.getBaselineValue("draupnir_spear.attack_damage_baseline", 8.0F);

            if (adBaseline > 0.0F) {
                double ad = living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > 0.0) factor = (float)(ad / adBaseline);
            }
        }

        return target.damage(source, amount * factor);
    }

    @Redirect(
            method = "detonate()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;DDD F Z Lnet/minecraft/world/World$ExplosionSourceType;)Lnet/minecraft/world/explosion/Explosion;"
            ),
            require = 0
    )
    private Explosion tmscompat$scaleDetonate(World world, Entity source, double x, double y, double z,
                                              float power, boolean createFire, World.ExplosionSourceType type) {
        float factor = 1.0F;

        if (source instanceof LivingEntity living) {
            float adBaseline = ConfigHelper.getBaselineValue("draupnir_spear.attack_damage_baseline", 8.0F);

            if (adBaseline > 0.0F) {
                double ad = living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > 0.0) factor = (float)(ad / adBaseline);
            }
        }

        float scaled = power * factor;
        return world.createExplosion(source, x, y, z, scaled, createFire, type);
    }
}