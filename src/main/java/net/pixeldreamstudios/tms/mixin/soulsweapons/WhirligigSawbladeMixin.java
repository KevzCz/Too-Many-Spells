package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.items.sword.WhirligigSawblade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(WhirligigSawblade.class)
public abstract class WhirligigSawbladeMixin {
    @Unique
    private static float tmscompat$scale(LivingEntity user) {
        if (user == null) return 1.0F;

        float adBaseline = ConfigHelper.getBaselineValue("whirligig_sawblade.attack_damage_baseline", 11.0F);

        if (adBaseline <= 0.0F) return 1.0F;

        double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (ad <= 0.0) return 1.0F;

        return (float)(ad / adBaseline);
    }

    @Redirect(
            method = "usageTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            )
    )
    private boolean tmscompat$scaleAbilityDamageOnly(LivingEntity target,
                                                     DamageSource source,
                                                     float originalAmount,
                                                     World world,
                                                     LivingEntity user,
                                                     ItemStack stack,
                                                     int remainingUseTicks) {
        float factor = tmscompat$scale(user);
        float ability = (float) ConfigConstructor.whirligig_sawblade_ability_damage * factor;
        float ench = 0.0F;
        if (world instanceof ServerWorld serverWorld) {
            ench = EnchantmentHelper.getDamage(serverWorld, stack, target, source, 0.0F);
        }
        float newAmount = ability + ench;
        return target.damage(source, newAmount);
    }
}