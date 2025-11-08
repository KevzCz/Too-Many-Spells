package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.entity.projectile.noclip.DamagingNoClipEntity;
import net.soulsweaponry.entity.projectile.noclip.GhostGlaiveEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(value = DamagingNoClipEntity.class, remap = false)
public abstract class GhostGlaiveMixin {

    @Unique
    private float tmscompat$computeFactor() {
        if (!(((Object) this) instanceof GhostGlaiveEntity gg)) return 1.0F;

        Entity owner = gg.getOwner();
        if (!(owner instanceof LivingEntity living)) return 1.0F;

        float adBaseline = ConfigHelper.getBaselineValue("ghost_glaive.attack_damage_baseline", 10.0F);
        float arcaneBaseline = ConfigHelper.getBaselineValue("ghost_glaive.arcane_baseline", 20.0F);
        float adWeight = ConfigHelper.getFloatValue("ghost_glaive.attack_damage_weight", 0.5F);
        float arcaneWeight = ConfigHelper.getFloatValue("ghost_glaive.arcane_weight", 0.5F);

        float ad = (float) living.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        float arcane = 0.0F;

        RegistryEntry.Reference<EntityAttribute> arcaneRef = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.ARCANE);
        if (arcaneRef != null) {
            arcane = (float) living.getAttributeValue(arcaneRef);
        }

        float adPart = adBaseline > 0.0F ? ad / adBaseline : 1.0F;
        float arcanePart = arcaneBaseline > 0.0F ? arcane / arcaneBaseline : 0.0F;

        float factor = adWeight * adPart + arcaneWeight * arcanePart;
        return Math.max(0.0F, factor);
    }

    @ModifyArg(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z",
                    remap = true
            ),
            index = 1,
            require = 0
    )
    private float tmscompat$scaleGhostGlaiveDamage(float amount) {
        return amount * tmscompat$computeFactor();
    }
}