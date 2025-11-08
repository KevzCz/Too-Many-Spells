package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.items.sword.EmpoweredDawnbreaker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = EmpoweredDawnbreaker.class)
public abstract class EmpoweredDawnbreakerMixin {
    @Unique private static final ThreadLocal<Float> tmscompat$scale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "summonFlamePillars(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void tmscompat$cacheFireScale(World world, ItemStack stack, LivingEntity user, CallbackInfo ci) {
        float factor = 1.0F;
        if (user != null) {
            float fireBaseline = ConfigHelper.getBaselineValue("empowered_dawnbreaker.fire_baseline", 20.0F);

            if (fireBaseline > 0.0F) {
                RegistryEntry.Reference<EntityAttribute> ref = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.FIRE);
                double fire = (ref != null) ? user.getAttributeValue(ref) : 0.0;
                factor = 1.0F + (float)(fire / fireBaseline);
            }
        }
        tmscompat$scale.set(factor);
    }

    @ModifyArg(
            method = "summonFlamePillars(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/noclip/FlamePillar;setDamage(D)V",
                    remap = false
            ),
            index = 0,
            require = 0
    )
    private double tmscompat$scaleFlamePillarDamage(double baseDamage) {
        return baseDamage * tmscompat$scale.get();
    }

    @Inject(
            method = "summonFlamePillars(Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void tmscompat$clearFireScale(World world, ItemStack stack, LivingEntity user, CallbackInfo ci) {
        tmscompat$scale.remove();
    }
}