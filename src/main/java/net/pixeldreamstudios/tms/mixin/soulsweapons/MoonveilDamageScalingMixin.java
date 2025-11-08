package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.soulsweaponry.items.katana.Moonveil;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = Moonveil.class)
public abstract class MoonveilDamageScalingMixin {
    @Unique private static final ThreadLocal<Float> tmscompat$scale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At("HEAD"),
            require = 0
    )
    private void tmscompat$cacheScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        float factor = 1.0F;
        if (user != null) {
            float adBaseline = ConfigHelper.getBaselineValue("moonveil.attack_damage_baseline", 11.0F);

            if (adBaseline > 0.0F) {
                double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > 0.0) factor = (float)(ad / adBaseline);
            }
        }
        tmscompat$scale.set(factor);
    }

    @Redirect(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETSTATIC,
                    target = "Lnet/soulsweaponry/config/ConfigConstructor;moonveil_vertical_damage:F",
                    remap = false
            ),
            require = 0
    )
    private float tmscompat$scaleVerticalField() {
        return net.soulsweaponry.config.ConfigConstructor.moonveil_vertical_damage * tmscompat$scale.get();
    }

    @Redirect(
            method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETSTATIC,
                    target = "Lnet/soulsweaponry/config/ConfigConstructor;moonveil_wave_damage:F",
                    remap = false
            ),
            require = 0
    )
    private float tmscompat$scaleWaveField() {
        return net.soulsweaponry.config.ConfigConstructor.moonveil_wave_damage * tmscompat$scale.get();
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