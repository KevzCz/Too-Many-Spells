package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.soulsweaponry.entitydata.UmbralTrespassData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(value = UmbralTrespassData.class)
public abstract class UmbralTrespassDataAttackerMixin {

    @ModifyArg(
            method = "*",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            ),
            index = 0,
            require = 0
    )
    private static DamageSource tmscompat$forcePlayerAttackSource(DamageSource original) {
        Entity attacker = original.getAttacker();
        if (attacker instanceof PlayerEntity player) {
            return player.getDamageSources().playerAttack(player);
        }
        return original;
    }
}
