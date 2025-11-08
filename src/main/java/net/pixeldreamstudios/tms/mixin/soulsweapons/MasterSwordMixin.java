package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.soulsweaponry.items.sword.MasterSword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(MasterSword.class)
public abstract class MasterSwordMixin {
    @Unique private static final ThreadLocal<Float> tmscompat$factor = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD")
    )
    private void tmscompat$cacheScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        float factor = 1.0F;
        if (user != null) {
            float adBaseline = ConfigHelper.getBaselineValue("master_sword.attack_damage_baseline", 8.0F);
            float hpBaseline = ConfigHelper.getBaselineValue("master_sword.max_health_baseline", 40.0F);
            float adWeight = ConfigHelper.getFloatValue("master_sword.attack_damage_weight", 0.5F);
            float hpWeight = ConfigHelper.getFloatValue("master_sword.max_health_weight", 0.5F);

            RegistryEntry<EntityAttribute> adEntry = EntityAttributes.GENERIC_ATTACK_DAMAGE;
            RegistryEntry<EntityAttribute> hpEntry = EntityAttributes.GENERIC_MAX_HEALTH;

            double ad = user.getAttributeValue(adEntry);
            double hp = user.getAttributeValue(hpEntry);

            float adPart = adBaseline > 0.0F ? (float)(ad / adBaseline) : 1.0F;
            float hpPart = hpBaseline > 0.0F ? (float)(hp / hpBaseline) : 1.0F;

            factor = adWeight * adPart + hpWeight * hpPart;
            if (factor < 0.0F) factor = 0.0F;
        }
        tmscompat$factor.set(factor);
    }

    @ModifyArg(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/MoonlightProjectile;setDamage(D)V"
            ),
            index = 0
    )
    private double tmscompat$scaleProjectileDamage(double baseDamage) {
        return baseDamage * tmscompat$factor.get();
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("TAIL")
    )
    private void tmscompat$clearScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        tmscompat$factor.remove();
    }
}