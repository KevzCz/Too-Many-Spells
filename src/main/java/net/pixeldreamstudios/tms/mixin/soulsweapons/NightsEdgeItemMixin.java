package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.items.sword.NightsEdgeItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(NightsEdgeItem.class)
public abstract class NightsEdgeItemMixin {
    @Unique private static final ThreadLocal<Float> tmscompat$scale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "spawnNightsEdge(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Vec3d;IF)V",
            at = @At("HEAD")
    )
    private void tmscompat$cacheScale(World world, LivingEntity user, ItemStack stack, Vec3d position, int warmup, float yaw, CallbackInfo ci) {
        float factor = 1.0F;
        if (user != null) {
            float adBaseline = ConfigHelper.getBaselineValue("nights_edge.attack_damage_baseline", 10.0F);
            float arcaneBaseline = ConfigHelper.getBaselineValue("nights_edge.arcane_baseline", 20.0F);
            float adWeight = ConfigHelper.getFloatValue("nights_edge.attack_damage_weight", 0.5F);
            float arcaneWeight = ConfigHelper.getFloatValue("nights_edge.arcane_weight", 0.5F);

            double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            RegistryEntry.Reference<EntityAttribute> arcaneEntry = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.ARCANE);
            double arcane = arcaneEntry != null ? user.getAttributeValue(arcaneEntry) : 0.0D;

            float adPart = adBaseline > 0.0F ? (float)(ad / adBaseline) : 1.0F;
            float arcanePart = arcaneBaseline > 0.0F ? (float)(arcane / arcaneBaseline) : 0.0F;
            factor = adWeight * adPart + arcaneWeight * arcanePart;
            if (factor < 0.0F) factor = 0.0F;
        }
        tmscompat$scale.set(factor);
    }

    @ModifyArg(
            method = "spawnNightsEdge(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Vec3d;IF)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/NightsEdge;setDamage(F)V"
            ),
            index = 0
    )
    private float tmscompat$scaleAbilityDamage(float originalArg) {
        float base = (float) ConfigConstructor.nights_edge_ability_damage;
        float ench = originalArg - base;
        float factor = tmscompat$scale.get();
        return base * factor + ench;
    }

    @Inject(
            method = "spawnNightsEdge(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/Vec3d;IF)V",
            at = @At("TAIL")
    )
    private void tmscompat$clearScale(World world, LivingEntity user, ItemStack stack, Vec3d position, int warmup, float yaw, CallbackInfo ci) {
        tmscompat$scale.remove();
    }
}