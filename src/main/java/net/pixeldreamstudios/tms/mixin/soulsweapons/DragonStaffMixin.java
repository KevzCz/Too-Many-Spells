package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.items.staff.DragonStaff;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(DragonStaff.class)
public abstract class DragonStaffMixin {

    @Unique private static final ThreadLocal<Float> tmscompat$fogHeal =
            ThreadLocal.withInitial(() -> ConfigConstructor.dragon_staff_vigorous_fog_damage_and_heal);
    @Unique private static final ThreadLocal<Float> tmscompat$fogDamage =
            ThreadLocal.withInitial(() -> ConfigConstructor.dragon_staff_vigorous_fog_damage_and_heal);
    @Unique private static final ThreadLocal<Float> tmscompat$auraAmp =
            ThreadLocal.withInitial(() -> ConfigConstructor.dragon_staff_aura_strength);

    @Inject(method = "usageTick", at = @At("HEAD"))
    private void tmscompat$cacheArcaneScaling(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        float arcane = 0.0F;
        if (user != null) {
            RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.ARCANE);
            if (entry != null) {
                arcane = (float) user.getAttributeValue(entry);
            }
        }

        float arcaneBaseline = ConfigHelper.getBaselineValue("dragon_staff.arcane_baseline", 20.0F);
        float healCap = ConfigHelper.getBaselineValue("dragon_staff.heal_cap_multiplier", 1.25F);
        float auraPer10 = ConfigHelper.getBaselineValue("dragon_staff.aura_amplifier_per_10_arcane", 1.0F);

        float baseFog = ConfigConstructor.dragon_staff_vigorous_fog_damage_and_heal;
        float factor = arcaneBaseline > 0.0F ? 1.0F + (arcane / arcaneBaseline) : 1.0F;

        tmscompat$fogDamage.set(baseFog * factor);
        tmscompat$fogHeal.set(baseFog * Math.min(healCap, factor));
        tmscompat$auraAmp.set(ConfigConstructor.dragon_staff_aura_strength + (arcane / 10.0F) * auraPer10);
    }

    @Inject(method = "usageTick", at = @At("TAIL"))
    private void tmscompat$clearArcaneScaling(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        tmscompat$fogHeal.remove();
        tmscompat$fogDamage.remove();
        tmscompat$auraAmp.remove();
    }

    @Redirect(
            method = "usageTick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/soulsweaponry/config/ConfigConstructor;dragon_staff_vigorous_fog_damage_and_heal:F",
                    ordinal = 0,
                    remap = false
            ),
            require = 0
    )
    private float tmscompat$redirectFogHeal() {
        return tmscompat$fogHeal.get();
    }

    @Redirect(
            method = "usageTick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/soulsweaponry/config/ConfigConstructor;dragon_staff_vigorous_fog_damage_and_heal:F",
                    ordinal = 1,
                    remap = false
            ),
            require = 0
    )
    private float tmscompat$redirectFogDamage() {
        return tmscompat$fogDamage.get();
    }

    @Redirect(
            method = "usageTick",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/soulsweaponry/config/ConfigConstructor;dragon_staff_aura_strength:F",
                    remap = false
            ),
            require = 0
    )
    private float tmscompat$redirectAuraAmp() {
        return tmscompat$auraAmp.get();
    }
}