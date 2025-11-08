package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.soulsweaponry.entitydata.UmbralTrespassData;
import net.soulsweaponry.items.scythe.DarkinScythePrime;
import net.soulsweaponry.items.scythe.ShadowAssassinScythe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = UmbralTrespassData.class)
public abstract class UmbralTrespassMixin {
    @Unique private static final ThreadLocal<Float> FACTOR = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "setOtherStats(Lnet/minecraft/entity/LivingEntity;FIZ)V",
            at = @At("HEAD"),
            require = 0
    )
    private static void tmscompat$cacheFactor(LivingEntity user, float damage, int cooldown, boolean heal, CallbackInfo ci) {
        float baseline = 0.0F;

        ItemStack main = user.getMainHandStack();
        Item mItem = main.getItem();
        if (mItem instanceof DarkinScythePrime) {
            baseline = ConfigHelper.getBaselineValue("umbral_trespass.darkin_scythe_baseline", 12.0F);
        } else if (mItem instanceof ShadowAssassinScythe) {
            baseline = ConfigHelper.getBaselineValue("umbral_trespass.shadow_assassin_scythe_baseline", 13.0F);
        }

        if (baseline == 0.0F) {
            ItemStack off = user.getOffHandStack();
            Item oItem = off.getItem();
            if (oItem instanceof DarkinScythePrime) {
                baseline = ConfigHelper.getBaselineValue("umbral_trespass.darkin_scythe_baseline", 12.0F);
            } else if (oItem instanceof ShadowAssassinScythe) {
                baseline = ConfigHelper.getBaselineValue("umbral_trespass.shadow_assassin_scythe_baseline", 13.0F);
            }
        }

        float factor = 1.0F;
        if (baseline > 0.0F) {
            double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            if (ad > 0.0) factor = (float)(ad / baseline);
        }
        FACTOR.set(factor);
    }

    @ModifyVariable(
            method = "setOtherStats(Lnet/minecraft/entity/LivingEntity;FIZ)V",
            at = @At("HEAD"),
            index = 1,
            argsOnly = true,
            require = 0
    )
    private static float tmscompat$scaleTrespassDamage(float originalDamage) {
        float scaled = originalDamage * FACTOR.get();
        return scaled;
    }

    @Inject(
            method = "setOtherStats(Lnet/minecraft/entity/LivingEntity;FIZ)V",
            at = @At("TAIL"),
            require = 0
    )
    private static void tmscompat$clear(LivingEntity user, float damage, int cooldown, boolean heal, CallbackInfo ci) {
        FACTOR.remove();
    }
}