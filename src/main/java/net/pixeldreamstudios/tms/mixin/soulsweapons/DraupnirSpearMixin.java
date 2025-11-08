package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.soulsweaponry.items.spear.DraupnirSpear;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(value = DraupnirSpear.class)
public abstract class DraupnirSpearMixin {

    @Redirect(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            ),
            require = 0
    )
    private boolean tmscompat$scaleAOEDamage(Entity target, DamageSource source, float amount,
                                             ServerWorld world, ItemStack stack, PlayerEntity player) {
        float factor = 1.0F;
        if (player != null) {
            float adBaseline = ConfigHelper.getBaselineValue("draupnir_spear.attack_damage_baseline", 8.0F);

            if (adBaseline > 0.0F) {
                double ad = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > 0.0) factor = (float)(ad / adBaseline);
            }
        }
        return target.damage(source, amount * factor);
    }
}