package net.pixeldreamstudios.tms.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.pixeldreamstudios.tms.TooManySpells;
import net.pixeldreamstudios.tms.util.ExtendedFreyrSwordData;
import net.pixeldreamstudios.tms.util.SummonTracker;
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

            if (SummonTracker.clientIsSpellSummon(entity.getUuid())) {
                TooManySpells.LOGGER.info("[CLIENT] Blocking interaction with spell summon");
                cir.setReturnValue(ActionResult.SUCCESS);
            }
        } else {

            if (ExtendedFreyrSwordData.isSpellSummon(player, entity.getUuid())) {
                TooManySpells.LOGGER.info("[SERVER] Blocking interaction with spell summon");
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


        if (isAnyPlayerSpellSummon(entity)) {
            TooManySpells.LOGGER.info("Preventing drop for spell summon");
            ci.cancel();
        }
    }

    @Inject(method = "insertStack", at = @At("HEAD"), cancellable = true)
    private void preventSpellSummonInsert(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        FreyrSwordEntity entity = (FreyrSwordEntity) (Object) this;

        if (ExtendedFreyrSwordData.isSpellSummon(player, entity.getUuid())) {
            TooManySpells.LOGGER.info("Preventing insert for spell summon");
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
            if (ExtendedFreyrSwordData.isSpellSummon(attacker, entity.getUuid())) {
                TooManySpells.LOGGER.info("Blocking owner damage to spell summon");
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

        if (entity.getOwnerUuid() != null) {
            PlayerEntity owner = entity.getWorld().getPlayerByUuid(entity.getOwnerUuid());
            if (owner != null) {
                ExtendedFreyrSwordData.unregisterSpellSummon(owner, entity.getUuid()); // ← UPDATED
            }
        }
    }

    private boolean isAnyPlayerSpellSummon(FreyrSwordEntity entity) {
        if (entity.getOwnerUuid() != null) {
            PlayerEntity owner = entity.getWorld().getPlayerByUuid(entity.getOwnerUuid());
            if (owner != null) {
                return ExtendedFreyrSwordData.isSpellSummon(owner, entity.getUuid());
            }
        }
        return false;
    }
}