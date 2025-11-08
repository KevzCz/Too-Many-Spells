package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.items.sword.LichBane;
import net.soulsweaponry.mixin.LivingEntityInvoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(LichBane.class)
public abstract class LichBaneMixin {
    @Unique private static final ThreadLocal<Float> tmscompat$factor = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(method = "postHit", at = @At("HEAD"))
    private void tmscompat$cacheFactor(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        float factor = 1.0F;
        if (attacker != null) {
            double fire = 0.0D;

            RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.FIRE);
            if (entry != null) {
                fire = attacker.getAttributeValue(entry);
            }

            float fireBaseline = ConfigHelper.getBaselineValue("lich_bane.fire_baseline", 20.0F);
            factor = 1.0F + (float)(fire / fireBaseline);
            if (factor < 0.0F) factor = 0.0F;
        }
        tmscompat$factor.set(factor);
    }

    @Inject(method = "postHit", at = @At("TAIL"))
    private void tmscompat$clearFactor(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        tmscompat$factor.remove();
    }

    @Redirect(
            method = "postHit",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/mixin/LivingEntityInvoker;invokeApplyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V"
            )
    )
    private void tmscompat$scaleMagicDamage(LivingEntityInvoker inv, net.minecraft.entity.damage.DamageSource src, float amount) {
        float scaled = amount * tmscompat$factor.get();
        inv.invokeApplyDamage(src, scaled);
    }
}