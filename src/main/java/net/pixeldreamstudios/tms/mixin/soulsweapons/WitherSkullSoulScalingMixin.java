package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WitherSkullEntity.class)
public abstract class WitherSkullSoulScalingMixin {

    @Unique
    private static float tmscompat$factorFromSource(DamageSource src) {
        Entity attacker = src.getAttacker();
        if (!(attacker instanceof PlayerEntity player)) return 1.0F;

        RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.SOUL);
        if (entry == null) return 1.0F;

        double soul = ((LivingEntity) player).getAttributeValue(entry);
        float baseline = ConfigHelper.getBaselineValue("wither_skull.soul_baseline", 20.0F);
        return 1.0F + (float)(soul / baseline);
    }

    @Redirect(
            method = "onEntityHit(Lnet/minecraft/util/hit/EntityHitResult;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            )
    )
    private boolean tmscompat$scalePlayerWitherSkullDamage(Entity target, DamageSource source, float amount) {
        return target.damage(source, amount * tmscompat$factorFromSource(source));
    }
}