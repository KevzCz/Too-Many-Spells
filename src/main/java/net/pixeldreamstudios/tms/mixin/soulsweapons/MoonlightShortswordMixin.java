package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.soulsweaponry.entity.projectile.MoonlightProjectile;
import net.soulsweaponry.items.sword.MoonlightShortsword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(MoonlightShortsword.class)
public abstract class MoonlightShortswordMixin {
    @Unique private static final ThreadLocal<Float> tmscompat$scale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(method = "summonSmallProjectile", at = @At("HEAD"))
    private static void tmscompat$cacheScale(World world, PlayerEntity user, CallbackInfo ci) {
        float factor = 1.0F;
        if (user != null) {
            float baseline = ConfigHelper.getBaselineValue("moonlight_shortsword.attack_damage_baseline", 8.0F);
            double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (ad > 0.0 && baseline > 0.0F) {
                factor = (float)(ad / baseline);
            }
        }
        tmscompat$scale.set(factor);
    }

    @Inject(method = "summonSmallProjectile", at = @At("TAIL"))
    private static void tmscompat$clearScale(World world, PlayerEntity user, CallbackInfo ci) {
        tmscompat$scale.remove();
    }

    @Redirect(
            method = "summonSmallProjectile",
            at = @At(value = "INVOKE", target = "Lnet/soulsweaponry/entity/projectile/MoonlightProjectile;setDamage(D)V")
    )
    private static void tmscompat$scaleShortswordDamage(MoonlightProjectile projectile, double damage) {
        projectile.setDamage(damage * tmscompat$scale.get());
    }
}