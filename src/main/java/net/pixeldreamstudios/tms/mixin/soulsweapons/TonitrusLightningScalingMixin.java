package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.items.hammer.Tonitrus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = Tonitrus.class)
public abstract class TonitrusLightningScalingMixin {

    @Unique private static final ThreadLocal<Float> tmscompat$damageScale = ThreadLocal.withInitial(() -> 1.0F);
    @Unique private static final ThreadLocal<Float> tmscompat$ampBonus   = ThreadLocal.withInitial(() -> 0.0F);
    @Unique private static final ThreadLocal<ServerPlayerEntity> tmscompat$channeler = new ThreadLocal<>();

    @Unique
    private static double tmscompat$getLightningPower(LivingEntity user) {
        if (user == null) return 0.0;
        RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.LIGHTNING);
        if (entry == null) return 0.0;
        return user.getAttributeValue(entry);
    }

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At("HEAD"),
            require = 0
    )
    private void tmscompat$cacheDamageScaleAndChanneler(net.minecraft.item.ItemStack stack,
                                                        LivingEntity target,
                                                        LivingEntity attacker,
                                                        CallbackInfoReturnable<Boolean> cir) {
        float scale = 1.0F;
        if (attacker != null) {
            float lightningBaseline = ConfigHelper.getBaselineValue("tonitrus.lightning_baseline", 40.0F);

            if (lightningBaseline > 0.0F) {
                double power = tmscompat$getLightningPower(attacker);
                scale = 1.0F + (float)(power / lightningBaseline);
            }

            if (attacker instanceof ServerPlayerEntity sp) {
                tmscompat$channeler.set(sp);
            } else {
                tmscompat$channeler.set(null);
            }
        }
        tmscompat$damageScale.set(scale);
    }

    @ModifyArg(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/soulsweaponry/items/abilities/ChainLightning;trigger(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;ZFD)V",
                    remap = false
            ),
            index = 4,
            require = 0
    )
    private float tmscompat$scaleChainLightningDamage(float baseDamage) {
        return baseDamage * tmscompat$damageScale.get();
    }

    @Redirect(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"
            ),
            require = 0
    )
    private boolean tmscompat$spawnWithChanneler(World world, Entity entity) {
        if (entity instanceof net.minecraft.entity.LightningEntity lightning) {
            ServerPlayerEntity sp = tmscompat$channeler.get();
            if (sp != null) {
                lightning.setChanneler(sp);
            }
        }
        return world.spawnEntity(entity);
    }

    @Inject(
            method = "postHit(Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;)Z",
            at = @At("RETURN"),
            require = 0
    )
    private void tmscompat$clearPostHitCaches(net.minecraft.item.ItemStack stack,
                                              LivingEntity target,
                                              LivingEntity attacker,
                                              CallbackInfoReturnable<Boolean> cir) {
        tmscompat$damageScale.remove();
        tmscompat$channeler.remove();
    }

    @Inject(
            method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",
            at = @At("HEAD"),
            require = 0
    )
    private void tmscompat$cacheAmpBonus(World world, PlayerEntity user, Hand hand,
                                         CallbackInfoReturnable<net.minecraft.util.TypedActionResult<net.minecraft.item.ItemStack>> cir) {
        float bonus = 0.0F;
        if (user != null) {
            float ampPer = ConfigHelper.getBaselineValue("tonitrus.amplifier_per_lightning", 20.0F);

            if (ampPer > 0.0F) {
                double power = tmscompat$getLightningPower(user);
                bonus = (float)(power / ampPer);
            }
        }
        tmscompat$ampBonus.set(bonus);
    }

    @ModifyArg(
            method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/math/MathHelper;ceil(F)I"
            ),
            index = 0,
            require = 0
    )
    private float tmscompat$addAmpBonus(float original) {
        return original + tmscompat$ampBonus.get();
    }

    @Inject(
            method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;",
            at = @At("RETURN"),
            require = 0
    )
    private void tmscompat$clearUseBonus(World world, PlayerEntity user, Hand hand,
                                         CallbackInfoReturnable<net.minecraft.util.TypedActionResult<net.minecraft.item.ItemStack>> cir) {
        tmscompat$ampBonus.remove();
    }
}