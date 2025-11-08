package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.soulsweaponry.items.sword.BluemoonShortsword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = BluemoonShortsword.class)
public abstract class BluemoonShortswordMixin {

    @Inject(method = "getProjectileDamage()F", at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private void tmscompat$scaleProjectileDamage(CallbackInfoReturnable<Float> cir) {
        float base = cir.getReturnValueF();
        float weaponAd = ((BluemoonShortsword) (Object) this).getAttackDamage();
        float baseline = ConfigHelper.getBaselineValue("bluemoon_shortsword.attack_damage_baseline", 7.0F);

        if (weaponAd > 0.0F && baseline > 0.0F) {
            float factor = weaponAd / baseline;
            cir.setReturnValue(base * factor);
        }
    }
}