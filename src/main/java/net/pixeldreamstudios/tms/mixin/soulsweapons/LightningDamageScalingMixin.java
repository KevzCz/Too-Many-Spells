package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class LightningDamageScalingMixin {
    @Unique
    private static final ThreadLocal<Float> tmscompat$add = ThreadLocal.withInitial(() -> 0.0F);

    @Inject(method = "onStruckByLightning", at = @At("HEAD"))
    private void tmscompat$cacheAdditiveBonus(ServerWorld world, LightningEntity lightning, CallbackInfo ci) {
        float add = 0.0F;
        ServerPlayerEntity sp = lightning.getChanneler();
        if (sp != null) {
            RegistryEntry.Reference<EntityAttribute> entry = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.LIGHTNING);
            if (entry != null) {
                double power = sp.getAttributeValue(entry);
                double damagePerPower = ConfigHelper.getDoubleValue("lightning.damage_per_spell_power", 0.5);
                add = (float) (power * damagePerPower);
            }
        }
        tmscompat$add.set(add);
    }

    @Redirect(
            method = "onStruckByLightning",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            )
    )
    private boolean tmscompat$customLightningDamage(Entity instance,
                                                    net.minecraft.entity.damage.DamageSource source,
                                                    float baseAmount,
                                                    ServerWorld world,
                                                    LightningEntity lightning) {
        ServerPlayerEntity channeler = lightning.getChanneler();

        if (channeler == null) {
            return instance.damage(source, baseAmount);
        }

        boolean damageChanneler = ConfigHelper.getBooleanValue("lightning.damage_channeler", false);
        if (instance == channeler && !damageChanneler) {
            return false;
        }

        boolean damagePassive = ConfigHelper.getBooleanValue("lightning.damage_passive_entities", false);
        if (instance instanceof PassiveEntity && !damagePassive) {
            return false;
        }

        boolean damageTamed = ConfigHelper.getBooleanValue("lightning.damage_tamed_entities", false);
        if (instance instanceof TameableEntity tame && tame.isTamed() && !damageTamed) {
            return false;
        }

        float amount = baseAmount + tmscompat$add.get();

        if (instance instanceof PlayerEntity) {
            float playerMultiplier = ConfigHelper.getFloatValue("lightning.player_damage_multiplier", 0.9F);
            amount *= playerMultiplier;
        }

        return instance.damage(source, amount);
    }

    @Inject(method = "onStruckByLightning", at = @At("RETURN"))
    private void tmscompat$clear(ServerWorld world, LightningEntity lightning, CallbackInfo ci) {
        tmscompat$add.remove();
    }
}