package net.pixeldreamstudios.tms.mixin.minecraft.entity;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.pixeldreamstudios.summonerlib.util.SummonControlUtil;
import net.pixeldreamstudios.tms.registry.TMSStatusEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMarkedForDeathMixin {

    @Inject(method = "damage", at = @At("HEAD"))
    private void tms$onMarkedForDeathDamaged(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (!self.hasStatusEffect(TMSStatusEffects.MARKED_FOR_DEATH)) {
            return;
        }

        if (!(source.getAttacker() instanceof PlayerEntity player)) {
            return;
        }

        SummonControlUtil.forceAllSummonsAttackTarget(player, self);
    }
}