package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.soulsweaponry.entity.projectile.MjolnirProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(MjolnirProjectile.class)
public abstract class MjolnirProjectileDamageMixin {
    @Unique private static final ThreadLocal<Float> tmscompat$factor = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Boolean> tmscompat$didScale = ThreadLocal.withInitial(() -> false);

    @Inject(method = "getDamage(Lnet/minecraft/entity/Entity;)F", at = @At("HEAD"))
    private void tmscompat$cacheFactor(Entity target, CallbackInfoReturnable<Float> cir) {
        float factor = 1.0F;
        MjolnirProjectile self = (MjolnirProjectile) (Object) this;
        Entity owner = self.getOwner();
        if (owner instanceof LivingEntity living) {
            float adBaseline = ConfigHelper.getBaselineValue("mjolnir.projectile.attack_damage_baseline", 13.0F);
            if (adBaseline > 0.0F) {
                double ad = living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > 0.0) {
                    factor = (float) (ad / adBaseline);
                }
            }
        }
        tmscompat$factor.set(Math.max(0.0F, factor));
        tmscompat$didScale.set(false);
    }

    @Redirect(
            method = "getDamage(Lnet/minecraft/entity/Entity;)F",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/enchantment/EnchantmentHelper;getDamage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;F)F"
            )
    )
    private float tmscompat$scaleBaseForEnchant(ServerWorld serverWorld, ItemStack stack, Entity target, net.minecraft.entity.damage.DamageSource src, float base) {
        tmscompat$didScale.set(true);
        return EnchantmentHelper.getDamage(serverWorld, stack, target, src, base * tmscompat$factor.get());
    }

    @Inject(method = "getDamage(Lnet/minecraft/entity/Entity;)F", at = @At("RETURN"), cancellable = true)
    private void tmscompat$scaleWhenNoEnchantPath(Entity target, CallbackInfoReturnable<Float> cir) {
        if (!tmscompat$didScale.get()) {
            cir.setReturnValue(cir.getReturnValueF() * tmscompat$factor.get());
        }
        tmscompat$factor.remove();
        tmscompat$didScale.remove();
    }
}