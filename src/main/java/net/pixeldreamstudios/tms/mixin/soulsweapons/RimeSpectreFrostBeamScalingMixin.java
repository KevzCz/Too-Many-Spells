package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net.soulsweaponry.entity.mobs.RimeSpectre$RimeSpectreGoal")
public abstract class RimeSpectreFrostBeamScalingMixin {

    @Unique
    private static float tmscompat$factorFromSource(DamageSource src) {
        Entity attacker = src.getAttacker();
        if (!(attacker instanceof LivingEntity living)) return 1.0F;

        float soulBaseline = ConfigHelper.getBaselineValue("rime_spectre.soul_baseline", 20.0F);
        float frostBaseline = ConfigHelper.getBaselineValue("rime_spectre.frost_baseline", 20.0F);
        float soulWeight = ConfigHelper.getFloatValue("rime_spectre.soul_weight", 0.25F);
        float frostWeight = ConfigHelper.getFloatValue("rime_spectre.frost_weight", 0.75F);

        float soul = 0.0F;
        float frost = 0.0F;

        RegistryEntry.Reference<EntityAttribute> soulEntry = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.SOUL);
        if (soulEntry != null) soul = (float) living.getAttributeValue(soulEntry);

        RegistryEntry.Reference<EntityAttribute> frostEntry = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.FROST);
        if (frostEntry != null) frost = (float) living.getAttributeValue(frostEntry);

        float soulPart = soulBaseline > 0.0F ? soul / soulBaseline : 0.0F;
        float frostPart = frostBaseline > 0.0F ? frost / frostBaseline : 0.0F;
        float weighted = soulWeight * soulPart + frostWeight * frostPart;

        return 1.0F + weighted;
    }

    @Redirect(
            method = "frostBeam(Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"),
            require = 0
    )
    private boolean tmscompat$scaleRimeSpectreBeamDamage(LivingEntity target, DamageSource source, float amount) {
        return target.damage(source, amount * tmscompat$factorFromSource(source));
    }
}