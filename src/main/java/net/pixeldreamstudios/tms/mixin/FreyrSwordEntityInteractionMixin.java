package net.pixeldreamstudios.tms.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.pixeldreamstudios.summonerlib.tracker.ClientSummonTracker;
import net.pixeldreamstudios.tms.util.ExtendedFreyrSwordData;
import net.soulsweaponry.entity.mobs.FreyrSwordEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FreyrSwordEntity.class)
public class FreyrSwordEntityInteractionMixin {

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void preventSpellSummonInteraction(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        FreyrSwordEntity entity = (FreyrSwordEntity) (Object) this;

        if (entity.getWorld().isClient()) {
            if (ClientSummonTracker.isSpellSummon(entity.getUuid())) {
                cir.setReturnValue(ActionResult.SUCCESS);
            }
        } else {
            if (ExtendedFreyrSwordData.isSpellSummon(entity.getUuid())) {
                cir.setReturnValue(ActionResult.PASS);
            }
        }
    }

    @Inject(method = "dropStack()V", at = @At("HEAD"), cancellable = true)
    private void preventSpellSummonDropStack(CallbackInfo ci) {
        FreyrSwordEntity entity = (FreyrSwordEntity) (Object) this;

        if (entity.getWorld().isClient()) {
            return;
        }

        if (ExtendedFreyrSwordData.isSpellSummon(entity.getUuid())) {
            ci.cancel();
        }
    }

    @Inject(method = "insertStack", at = @At("HEAD"), cancellable = true)
    private void preventSpellSummonInsert(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        FreyrSwordEntity entity = (FreyrSwordEntity) (Object) this;

        if (ExtendedFreyrSwordData.isSpellSummon(entity.getUuid())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void preventOwnerDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        FreyrSwordEntity entity = (FreyrSwordEntity) (Object) this;

        if (entity.getWorld().isClient()) {
            return;
        }

        if (source.getAttacker() instanceof PlayerEntity attacker) {
            if (ExtendedFreyrSwordData.isSpellSummon(entity.getUuid())) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void cleanupSpellSummonOnDeath(DamageSource damageSource, CallbackInfo ci) {
        FreyrSwordEntity entity = (FreyrSwordEntity) (Object) this;

        if (entity.getWorld().isClient()) {
            return;
        }

        // Unregister from tracking
        if (entity.getOwnerUuid() != null) {
            PlayerEntity owner = entity.getWorld().getPlayerByUuid(entity.getOwnerUuid());
            if (owner != null && ExtendedFreyrSwordData.isSpellSummon(entity.getUuid())) {
                ExtendedFreyrSwordData.unregisterSpellSummon(owner, entity.getUuid());
            }
        }
    }

    // Prevent dropping items on death
    @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/soulsweaponry/entity/mobs/FreyrSwordEntity;insertStack(Lnet/minecraft/entity/player/PlayerEntity;)Z"), cancellable = true)
    private void preventSpellSummonDropOnDeath(DamageSource damageSource, CallbackInfo ci) {
        FreyrSwordEntity entity = (FreyrSwordEntity) (Object) this;

        if (ExtendedFreyrSwordData.isSpellSummon(entity.getUuid())) {
            ci.cancel(); // Skip the entire drop/insert logic
        }
    }
}