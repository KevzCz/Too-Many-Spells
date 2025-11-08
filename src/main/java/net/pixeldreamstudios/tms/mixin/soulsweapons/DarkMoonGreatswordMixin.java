package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.items.sword.DarkMoonGreatsword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(DarkMoonGreatsword.class)
public abstract class DarkMoonGreatswordMixin {
    @Unique private static final ThreadLocal<Float> tmscompat$damageFactor = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Integer> tmscompat$ampBonus = ThreadLocal.withInitial(() -> 0);

    @Inject(method = "onStoppedUsing", at = @At("HEAD"))
    private void tmscompat$cacheScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        float finalFactor = 1.0F;
        int bonusAmp = 0;

        if (user != null) {
            float adHalf = 1.0F;
            float baseAd = ((DarkMoonGreatsword)(Object)this).getAttackDamage();
            if (baseAd > 0.0F) {
                double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > 0.0) {
                    float full = (float)(ad / baseAd);
                    adHalf = 1.0F + 0.5F * (full - 1.0F);
                }
            }

            float frostHalf = 1.0F;
            double frostVal = 0.0;
            RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.FROST);
            if (entry != null) {
                frostVal = user.getAttributeValue(entry);
                float frostBaseline = ConfigHelper.getBaselineValue("dark_moon_greatsword.frost_baseline", 20.0F);
                frostHalf = 1.0F + 0.5F * ((float)frostVal / frostBaseline);
            }

            finalFactor = adHalf * frostHalf;
            float frostPerAmp = ConfigHelper.getBaselineValue("dark_moon_greatsword.frost_per_amplifier", 10.0F);
            bonusAmp = Math.max(0, (int)Math.floor(frostVal / frostPerAmp));
        }

        tmscompat$damageFactor.set(finalFactor);
        tmscompat$ampBonus.set(bonusAmp);
    }

    @ModifyArg(
            method = "onStoppedUsing",
            at = @At(value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/MoonlightProjectile;setDamage(D)V"),
            index = 0
    )
    private double tmscompat$scaleProjectileDamage(double baseDamage) {
        return baseDamage * tmscompat$damageFactor.get();
    }

    @ModifyArg(
            method = "onStoppedUsing",
            at = @At(value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/MoonlightProjectile;setEffectAmplifier(I)V"),
            index = 0
    )
    private int tmscompat$boostEffectAmplifier(int amp) {
        return amp + tmscompat$ampBonus.get();
    }

    @Inject(method = "onStoppedUsing", at = @At("TAIL"))
    private void tmscompat$clearScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        tmscompat$damageFactor.remove();
        tmscompat$ampBonus.remove();
    }
}