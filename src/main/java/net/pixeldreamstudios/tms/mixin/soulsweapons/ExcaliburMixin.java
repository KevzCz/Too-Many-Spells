package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.World;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.items.sword.Excalibur;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = Excalibur.class)
public abstract class ExcaliburMixin {
    @Unique private static final ThreadLocal<Float> tmscompat$scale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD"),
            require = 0
    )
    private void tmscompat$cacheScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        float factor = 1.0F;
        if (user != null) {
            float arcaneBaseline = ConfigHelper.getBaselineValue("excalibur.arcane_baseline", 20.0F);
            float soulBaseline = ConfigHelper.getBaselineValue("excalibur.soul_baseline", 20.0F);
            float arcaneWeight = ConfigHelper.getFloatValue("excalibur.arcane_weight", 0.5F);
            float soulWeight = ConfigHelper.getFloatValue("excalibur.soul_weight", 0.5F);

            RegistryEntry.Reference<EntityAttribute> arcRef = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.ARCANE);
            RegistryEntry.Reference<EntityAttribute> soulRef = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.SOUL);

            double arc = arcRef != null ? user.getAttributeValue(arcRef) : 0.0;
            double soul = soulRef != null ? user.getAttributeValue(soulRef) : 0.0;

            float arcPart = arcaneBaseline > 0.0F ? (float)(arc / arcaneBaseline) : 0.0F;
            float soulPart = soulBaseline > 0.0F ? (float)(soul / soulBaseline) : 0.0F;

            factor = 1.0F + arcaneWeight * arcPart + soulWeight * soulPart;
        }
        tmscompat$scale.set(factor);
    }

    @ModifyArg(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            ),
            index = 1,
            require = 0
    )
    private float tmscompat$scaleSonicBoomDamage(float amount) {
        return amount * tmscompat$scale.get();
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("TAIL"),
            require = 0
    )
    private void tmscompat$clearScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        tmscompat$scale.remove();
    }
}