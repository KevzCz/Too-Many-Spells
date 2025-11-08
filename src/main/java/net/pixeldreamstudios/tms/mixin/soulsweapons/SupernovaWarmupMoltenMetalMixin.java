package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.entity.projectile.noclip.DamagingWarmupEntity;
import net.soulsweaponry.entity.projectile.noclip.DamagingWarmupEntityEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = DamagingWarmupEntityEvents.class, remap = false)
public abstract class SupernovaWarmupMoltenMetalMixin {
    @Unique private static final ThreadLocal<Float> tmscompat$mmFactor = ThreadLocal.withInitial(() -> 1.0F);

    @Unique
    private static float tmscompat$computeScale(Entity owner) {
        if (!(owner instanceof LivingEntity living)) return 1.0F;

        float fireBaseline = ConfigHelper.getBaselineValue("supernova.fire_baseline", 20.0F);
        float moltenScaling = ConfigHelper.getBaselineValue("supernova.molten_metal_scaling", 0.25F);

        RegistryEntry.Reference<EntityAttribute> fireRef = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.FIRE);
        double fire = (fireRef != null) ? living.getAttributeValue(fireRef) : 0.0;

        float firePart = fireBaseline > 0.0F ? (float)(fire / fireBaseline) : 0.0F;
        float s = 1.0F + moltenScaling * firePart;
        return s < 0.0F ? 0.0F : s;
    }

    @Inject(method = "*",
            at = @At("HEAD"),
            require = 0)
    private static void tmscompat$cacheFactor(DamagingWarmupEntity warm,
                                              DamagingWarmupEntityEvents.OtherAttributes attrs,
                                              CallbackInfo ci) {
        tmscompat$mmFactor.set(tmscompat$computeScale(warm.getOwner()));
    }

    @ModifyArg(method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/noclip/MoltenMetal;setDamage(D)V",
                    remap = false
            ),
            index = 0,
            require = 0)
    private static double tmscompat$scaleMoltenDamageD(double base) {
        return base * tmscompat$mmFactor.get();
    }

    @ModifyArg(method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/noclip/MoltenMetal;setDamage(F)V",
                    remap = false
            ),
            index = 0,
            require = 0)
    private static float tmscompat$scaleMoltenDamageF(float base) {
        return base * tmscompat$mmFactor.get();
    }

    @Inject(method = "*",
            at = @At("TAIL"),
            require = 0)
    private static void tmscompat$clearFactor(DamagingWarmupEntity warm,
                                              DamagingWarmupEntityEvents.OtherAttributes attrs,
                                              CallbackInfo ci) {
        tmscompat$mmFactor.remove();
    }
}