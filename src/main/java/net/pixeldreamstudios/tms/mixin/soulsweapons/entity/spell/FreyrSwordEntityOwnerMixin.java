package net.pixeldreamstudios.tms.mixin.soulsweapons.entity.spell;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.pixeldreamstudios.tms.util.soulsweapons.ExtendedFreyrSwordData;
import net.soulsweaponry.entity.mobs.FreyrSwordEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(FreyrSwordEntity.class)
public class FreyrSwordEntityOwnerMixin {

    @Inject(method = "getOwner", at = @At("RETURN"), cancellable = true)
    private void extendOwnerCheckForSpellSummons(CallbackInfoReturnable<@Nullable LivingEntity> cir) {
        FreyrSwordEntity entity = (FreyrSwordEntity) (Object) this;

        if (cir.getReturnValue() != null) {
            return;
        }

        UUID entityUuid = entity.getUuid();
        UUID ownerUuid = entity.getOwnerUuid();

        if (ownerUuid == null) {
            return;
        }

        LivingEntity potentialOwner = entity.getWorld().getPlayerByUuid(ownerUuid);
        if (potentialOwner instanceof PlayerEntity player) {
            if (ExtendedFreyrSwordData.isSpellSummon(player, entityUuid)) {
                cir.setReturnValue(player);
            }
        }
    }
}