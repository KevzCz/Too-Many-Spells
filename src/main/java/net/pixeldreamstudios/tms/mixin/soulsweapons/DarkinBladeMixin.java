package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.items.sword.DarkinBlade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(DarkinBlade.class)
public abstract class DarkinBladeMixin {

    @Unique private static final ThreadLocal<Float> tmscompat$healScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Float> tmscompat$abilityScale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(method = "postHit", at = @At("HEAD"))
    private void tmscompat$cacheHealScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        float factor = 1.0F;
        if (attacker != null) {
            float adBaseline = ConfigHelper.getBaselineValue("darkin_blade.attack_damage_baseline", 11.0F);

            if (adBaseline > 0.0F) {
                double ad = attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > 0.0) factor = (float)(ad / adBaseline);
            }
        }
        float minScale = ConfigHelper.getBaselineValue("darkin_blade.heal_min_scale", 0.75F);
        float maxScale = ConfigHelper.getBaselineValue("darkin_blade.heal_max_scale", 1.25F);
        float clamped = Math.max(minScale, Math.min(maxScale, factor));
        tmscompat$healScale.set(clamped);
    }

    @ModifyVariable(method = "postHit", at = @At("STORE"), ordinal = 0, require = 0)
    private float tmscompat$scaleHealing(float healing) {
        return healing * tmscompat$healScale.get();
    }

    @Inject(method = "postHit", at = @At("RETURN"))
    private void tmscompat$clearHealScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        tmscompat$healScale.remove();
    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"))
    private void tmscompat$cacheAbilityScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        float factor = 1.0F;
        if (user != null) {
            float adBaseline = ConfigHelper.getBaselineValue("darkin_blade.attack_damage_baseline", 11.0F);

            if (adBaseline > 0.0F) {
                double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > 0.0) factor = (float)(ad / adBaseline);
            }
        }
        tmscompat$abilityScale.set(factor);
    }

    @Inject(method = "onStoppedUsing", at = @At("TAIL"))
    private void tmscompat$clearAbilityScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        tmscompat$abilityScale.remove();
    }

    @Redirect(
            method = "onStoppedUsing",
            at = @At(value = "FIELD", target = "Lnet/soulsweaponry/config/ConfigConstructor;darkin_blade_ability_damage:F", remap = false),
            require = 0
    )
    private float tmscompat$scaleAbilityDamageField() {
        float base = ConfigConstructor.darkin_blade_ability_damage;
        return base * tmscompat$abilityScale.get();
    }

    @Redirect(
            method = "<init>",
            at = @At(value = "FIELD", target = "Lnet/soulsweaponry/config/ConfigConstructor;darkin_blade_calculated_fall_max_damage:F", remap = false),
            require = 0
    )
    private float tmscompat$scaleCalculatedFallMaxDamage() {
        float base = ConfigConstructor.darkin_blade_calculated_fall_max_damage;
        float adBaseline = ConfigHelper.getBaselineValue("darkin_blade.attack_damage_baseline", 11.0F);
        float weaponAd = ((DarkinBlade)(Object)this).getAttackDamage();
        float factor = (adBaseline > 0.0F && weaponAd > 0.0F) ? weaponAd / adBaseline : 1.0F;
        return base * factor;
    }
}