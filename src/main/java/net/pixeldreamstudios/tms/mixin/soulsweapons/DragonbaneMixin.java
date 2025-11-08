package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.items.katana.Dragonbane;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = Dragonbane.class)
public abstract class DragonbaneMixin {
    @Unique private static final ThreadLocal<Float> tmscompat$scale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At("HEAD"),
            require = 0
    )
    private void tmscompat$cacheScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        float factor = 1.0F;
        if (attacker != null) {
            float lightningBaseline = ConfigHelper.getBaselineValue("dragonbane.lightning_baseline", 20.0F);

            RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.LIGHTNING);
            if (entry != null && lightningBaseline > 0.0F) {
                double power = attacker.getAttributeValue(entry);
                factor = 1.0F + (float)(power / lightningBaseline);
            }
        }
        tmscompat$scale.set(factor);
    }

    @Redirect(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At(
                    value = "FIELD",
                    opcode = Opcodes.GETSTATIC,
                    target = "Lnet/soulsweaponry/config/ConfigConstructor;dragonbane_chain_lightning_damage_per_level:F",
                    remap = false
            ),
            require = 0
    )
    private float tmscompat$scaleDamagePerLevel() {
        return net.soulsweaponry.config.ConfigConstructor.dragonbane_chain_lightning_damage_per_level * tmscompat$scale.get();
    }

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At("RETURN"),
            require = 0
    )
    private void tmscompat$clearScale(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        tmscompat$scale.remove();
    }
}