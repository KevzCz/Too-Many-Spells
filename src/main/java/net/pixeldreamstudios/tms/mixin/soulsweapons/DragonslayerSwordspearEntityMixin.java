package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.soulsweaponry.entity.projectile.DragonslayerSwordspearEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(DragonslayerSwordspearEntity.class)
public abstract class DragonslayerSwordspearEntityMixin {
    @Redirect(
            method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V", // use full descriptor
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    remap = true
            )
    )
    private boolean tmscompat$damageWithADAndCredit(Entity target, DamageSource source, float amount) {
        DragonslayerSwordspearEntity self = (DragonslayerSwordspearEntity) (Object) this;
        Entity owner = self.getOwner();

        if (owner instanceof LivingEntity living) {
            double ad = living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (ad > 0.0) {
                amount = (float) ad;
            }
        }

        if (owner instanceof PlayerEntity player) {
            source = self.getWorld().getDamageSources().playerAttack(player);
        }

        return target.damage(source, amount);
    }
}
