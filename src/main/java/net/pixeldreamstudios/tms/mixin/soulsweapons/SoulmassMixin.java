package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.entity.mobs.Soulmass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.soulsweaponry.entity.mobs.Soulmass$SoulmassGoal")
public abstract class SoulmassMixin {

    @Unique
    private static float tmscompat$factorFromSource(DamageSource src) {
        Entity attacker = src.getAttacker();
        if (!(attacker instanceof Soulmass sm)) return 1.0F;

        float soulBaseline = ConfigHelper.getBaselineValue("soulmass.soul_baseline", 20.0F);

        RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.SOUL);

        if (entry == null || soulBaseline <= 0.0F) return 1.0F;

        double soul = sm.getAttributeValue(entry);
        return 1.0F + (float)(soul / soulBaseline);
    }

    @Redirect(
            method = "tick",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"),
            remap = true
    )
    private boolean tmscompat$scaleBeam(net.minecraft.entity.LivingEntity target, DamageSource source, float amount) {
        return target.damage(source, amount * tmscompat$factorFromSource(source));
    }
}