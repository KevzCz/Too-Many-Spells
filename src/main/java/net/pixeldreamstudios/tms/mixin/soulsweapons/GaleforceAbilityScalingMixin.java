package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.items.bow.Galeforce;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = Galeforce.class)
public abstract class GaleforceAbilityScalingMixin {

    @Unique
    private static final ThreadLocal<Double> tmscompat$abilityDamage = ThreadLocal.withInitial(() -> 0.0);

    @Inject(
            method = "shootArrow(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Vec3d;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void tmscompat$calculateAbilityDamage(ServerWorld world, ItemStack stack, ItemStack arrowStack,
                                                  PlayerEntity player, @Nullable Vec3d currentTargetPos, CallbackInfo ci) {
        double rangedDamage = 0.0;
        float rangedBaseline = ConfigHelper.getBaselineValue("galeforce.ranged_damage_baseline", 9.0F);

        if (player != null) {
            RegistryEntry.Reference<EntityAttribute> rangedAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.RangedWeapon.DAMAGE);
            if (rangedAttr != null) {
                rangedDamage = player.getAttributeValue(rangedAttr);
            }
        }

        double baseDamage = rangedDamage > 0.0 ? rangedDamage : ConfigConstructor.galeforce_damage;

        if (rangedDamage > 0.0 && rangedBaseline > 0.0F) {
            baseDamage = ConfigConstructor.galeforce_damage * (rangedDamage / rangedBaseline);
        }

        double damage = baseDamage / ConfigConstructor.galeforce_ability_velocity;

        tmscompat$abilityDamage.set(damage);
    }

    @ModifyArg(
            method = "shootArrow(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Vec3d;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/arrow/ChargedArrow;setDamage(D)V"
            ),
            index = 0,
            require = 0
    )
    private double tmscompat$useScaledAbilityDamage(double originalDamage) {
        double scaled = tmscompat$abilityDamage.get();
        return scaled > 0.0 ? scaled : originalDamage;
    }

    @Inject(
            method = "shootArrow(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Vec3d;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void tmscompat$clearAbilityDamage(ServerWorld world, ItemStack stack, ItemStack arrowStack,
                                              PlayerEntity player, @Nullable Vec3d currentTargetPos, CallbackInfo ci) {
        tmscompat$abilityDamage.remove();
    }
}