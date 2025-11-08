package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.entity.projectile.DragonStaffProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = DragonStaffProjectile.class)
public abstract class DragonStaffProjectileMixin {
    @Unique private static final ThreadLocal<Float> tmscompat$auraAmp =
            ThreadLocal.withInitial(() -> ConfigConstructor.dragon_staff_aura_strength);

    @Inject(method = "detonate", at = @At("HEAD"), remap = false)
    private void tmscompat$cacheArcaneScaling(CallbackInfo ci) {
        float baseAmp = ConfigConstructor.dragon_staff_aura_strength;
        float arcane = 0.0F;

        DragonStaffProjectile self = (DragonStaffProjectile)(Object)this;
        Entity owner = self.getOwner();
        if (owner instanceof LivingEntity living) {
            RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.ARCANE);
            if (entry != null) {
                arcane = (float) living.getAttributeValue(entry);
            }
        }

        float auraPer10 = ConfigHelper.getBaselineValue("dragon_staff.aura_amplifier_per_10_arcane", 1.0F);
        tmscompat$auraAmp.set(baseAmp + (arcane / 10.0F) * auraPer10);
    }

    @Inject(method = "detonate", at = @At("TAIL"), remap = false)
    private void tmscompat$clearArcaneScaling(CallbackInfo ci) {
        tmscompat$auraAmp.remove();
    }

    @Redirect(
            method = "detonate",
            at = @At(value = "FIELD",
                    target = "Lnet/soulsweaponry/config/ConfigConstructor;dragon_staff_aura_strength:F"),
            remap = false
    )
    private float tmscompat$redirectDetonateAuraAmp() {
        return tmscompat$auraAmp.get();
    }
}