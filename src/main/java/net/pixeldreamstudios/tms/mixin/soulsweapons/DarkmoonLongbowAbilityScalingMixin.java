package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.items.bow.DarkmoonLongbow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = DarkmoonLongbow.class)
public abstract class DarkmoonLongbowAbilityScalingMixin {

    @Unique
    private static final ThreadLocal<Float> tmscompat$scale = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void tmscompat$calculateScale(ServerWorld world, ItemStack stack, PlayerEntity player, CallbackInfo ci) {
        float factor = 1.0F;

        if (player != null) {
            float rangedBaseline = ConfigHelper.getBaselineValue("darkmoon_longbow.ranged_damage_baseline", 9.0F);
            float arcaneBaseline = ConfigHelper.getBaselineValue("darkmoon_longbow.arcane_baseline", 20.0F);
            float rangedWeight = ConfigHelper.getFloatValue("darkmoon_longbow.ranged_damage_weight", 0.05F);
            float arcaneWeight = ConfigHelper.getFloatValue("darkmoon_longbow.arcane_weight", 0.075F);

            double ranged = 0.0;
            double arcane = 0.0;

            RegistryEntry.Reference<EntityAttribute> rangedAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.RangedWeapon.DAMAGE);
            if (rangedAttr != null) {
                ranged = player.getAttributeValue(rangedAttr);
            }

            RegistryEntry.Reference<EntityAttribute> arcaneAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.ARCANE);
            if (arcaneAttr != null) {
                arcane = player.getAttributeValue(arcaneAttr);
            }

            float rangedPart = rangedBaseline > 0.0F ? (float)(ranged / rangedBaseline) : 1.0F;
            float arcanePart = arcaneBaseline > 0.0F ? (float)(arcane / arcaneBaseline) : 0.0F;

            factor = 1.0F + rangedWeight * rangedPart + arcaneWeight * arcanePart;
        }

        tmscompat$scale.set(factor);
    }

    @ModifyArg(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/entity/projectile/noclip/ArrowStormEntity;setDamage(D)V"
            ),
            index = 0,
            require = 0
    )
    private double tmscompat$scaleAbilityDamage(double baseDamage) {
        return baseDamage * tmscompat$scale.get();
    }

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void tmscompat$clearScale(ServerWorld world, ItemStack stack, PlayerEntity player, CallbackInfo ci) {
        tmscompat$scale.remove();
    }
}