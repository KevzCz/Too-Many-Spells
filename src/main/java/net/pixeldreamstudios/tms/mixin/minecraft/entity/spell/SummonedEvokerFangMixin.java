package net.pixeldreamstudios.tms.mixin.minecraft.entity.spell;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.tms.spell.handler.SummonEvokerFangsHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EvokerFangsEntity.class)
public abstract class SummonedEvokerFangMixin {

    @Shadow
    @org.jetbrains.annotations.Nullable
    public abstract LivingEntity getOwner();

    @Redirect(
            method = "damage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            )
    )
    private boolean tms$customDamage(LivingEntity target, DamageSource source, float originalDamage) {
        EvokerFangsEntity self = (EvokerFangsEntity) (Object) this;

        if (self instanceof SummonEvokerFangsHandler.SummonedEvokerFangEntity summonedFang) {
            float customDamage = summonedFang.getCustomDamage();
            boolean result = target.damage(source, customDamage);

            if (result && self.getWorld() instanceof ServerWorld serverWorld) {
                net.minecraft.enchantment.EnchantmentHelper.onTargetDamaged(serverWorld, target, source);
            }

            return result;
        }

        return target.damage(source, originalDamage);
    }
}