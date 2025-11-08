package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.soulsweaponry.items.katana.Bloodlust;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = Bloodlust.class)
public abstract class BloodlustMixin {
    @Unique private static final ThreadLocal<Float> tmscompat$factor = ThreadLocal.withInitial(() -> 1.0F);

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At("HEAD"),
            require = 0
    )
    private void tmscompat$cache(ServerWorld world, ItemStack stack, PlayerEntity player, CallbackInfo ci) {
        float f = 1.0F;
        if (player != null) {
            float adBaseline = ConfigHelper.getBaselineValue("bloodlust.attack_damage_baseline", 7.0F);

            if (adBaseline > 0.0F) {
                double ad = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
                if (ad > 0.0) f = (float)(ad / adBaseline);
            }
        }
        tmscompat$factor.set(f);
    }

    @Redirect(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"),
            require = 0
    )
    private boolean tmscompat$scaledSelfDamage(PlayerEntity player, DamageSource source, float baseAmount,
                                               ServerWorld world, ItemStack stack, PlayerEntity samePlayer) {
        float factor = tmscompat$factor.get();
        float scaled = baseAmount * factor;

        float capHearts = ConfigHelper.getBaselineValue("bloodlust.self_damage_cap_hearts", 12.0F);
        float capHealthPercent = ConfigHelper.getBaselineValue("bloodlust.self_damage_cap_health_percent", 0.5F);
        float capHalfHp = player.getMaxHealth() * capHealthPercent;

        float capped = Math.min(scaled, Math.min(capHearts, capHalfHp));
        return player.damage(source, capped);
    }

    @Redirect(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At(value = "NEW", target = "net/minecraft/entity/effect/StatusEffectInstance",ordinal = 0),
            require = 0
    )
    private StatusEffectInstance tmscompat$newBloodthirsty3(RegistryEntry<StatusEffect> effect, int duration, int amplifier) {
        float f = tmscompat$factor.get();
        int amp = Math.max(0, (int)Math.floor((amplifier + 1) * f) - 1);
        return new StatusEffectInstance(effect, duration, amp);
    }

    @Redirect(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At(value = "NEW", target = "net/minecraft/entity/effect/StatusEffectInstance",ordinal = 1),
            require = 0
    )
    private StatusEffectInstance tmscompat$newStrength3(RegistryEntry<StatusEffect> effect, int duration, int amplifier) {
        float f = tmscompat$factor.get();
        int amp = Math.max(0, (int)Math.floor((amplifier + 1) * f) - 1);
        return new StatusEffectInstance(effect, duration, amp);
    }

    @Inject(
            method = "useKeybindAbilityServer(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/PlayerEntity;)V",
            at = @At("TAIL"),
            require = 0
    )
    private void tmscompat$clear(ServerWorld world, ItemStack stack, PlayerEntity player, CallbackInfo ci) {
        tmscompat$factor.remove();
    }
}