package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.soulsweaponry.items.sword.BluemoonGreatsword;
import net.soulsweaponry.items.sword.MoonlightGreatsword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = MoonlightGreatsword.class)
public abstract class MoonlightGreatswordMixin {
    @Unique private static final ThreadLocal<Float> tmscompat$scale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD"),
            require = 0
    )
    private void tmscompat$cacheScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        boolean isBluemoon = (Object) this instanceof BluemoonGreatsword;
        float baseline = ConfigHelper.getBaselineValue(
                isBluemoon ? "bluemoon_greatsword.attack_damage_baseline" : "moonlight_greatsword.attack_damage_baseline",
                isBluemoon ? 8.0F : 9.0F
        );

        float factor = 1.0F;
        if (user != null && baseline > 0.0F) {
            double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (ad > 0.0) factor = (float)(ad / baseline);
        }
        tmscompat$scale.set(factor);
    }

    @Redirect(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/items/sword/MoonlightGreatsword;getProjectileDamage()F"
            ),
            require = 0
    )
    private float tmscompat$scaleProjectileDamageCall(MoonlightGreatsword self) {
        return self.getProjectileDamage() * tmscompat$scale.get();
    }

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("TAIL"),
            require = 0
    )
    private void tmscompat$clearScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        tmscompat$scale.remove();
    }
}