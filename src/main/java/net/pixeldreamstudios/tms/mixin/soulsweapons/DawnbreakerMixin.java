package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.items.sword.AbstractDawnbreaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = AbstractDawnbreaker.class, remap = false)
public abstract class DawnbreakerMixin {
    @Unique private static final ThreadLocal<Float> tmscompat$eventFactor = ThreadLocal.withInitial(() -> 1.0F);

    @Unique
    private static double tmscompat$getFirePower(LivingEntity user) {
        if (user == null) return 0.0;
        RegistryEntry.Reference<EntityAttribute> ref = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.FIRE);
        return ref == null ? 0.0 : user.getAttributeValue(ref);
    }

    @Inject(method = "dawnbreakerEvent", at = @At("HEAD"), require = 0)
    private static void tmscompat$cacheEventFactor(LivingEntity target, LivingEntity attacker, ItemStack stack, CallbackInfo ci) {
        float fireBaseline = ConfigHelper.getBaselineValue("dawnbreaker.fire_baseline", 20.0F);
        float factor = 1.0F;

        if (fireBaseline > 0.0F) {
            factor = 1.0F + (float)(tmscompat$getFirePower(attacker) / fireBaseline);
        }

        tmscompat$eventFactor.set(factor);
    }

    @Redirect(
            method = "dawnbreakerEvent",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    remap = true
            ),
            require = 0
    )
    private static boolean tmscompat$scaleDawnbreakerEventDamage(LivingEntity targetHit, DamageSource source, float amount) {
        float scaled = amount * tmscompat$eventFactor.get();
        return targetHit.damage(source, scaled);
    }

    @Inject(method = "dawnbreakerEvent", at = @At("TAIL"), require = 0)
    private static void tmscompat$clearEventFactor(LivingEntity target, LivingEntity attacker, ItemStack stack, CallbackInfo ci) {
        tmscompat$eventFactor.remove();
    }
}