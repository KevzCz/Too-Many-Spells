package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.items.hammer.Nightfall;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(Nightfall.class)
public abstract class NightfallMixin {
    @Unique
    private static float tmscompat$scale(LivingEntity attacker) {
        if (attacker == null) return 1.0F;

        float adBaseline = ConfigHelper.getBaselineValue("nightfall.attack_damage_baseline", 11.0F);
        float soulBaseline = ConfigHelper.getBaselineValue("nightfall.soul_baseline", 20.0F);
        float adWeight = ConfigHelper.getFloatValue("nightfall.attack_damage_weight", 0.5F);
        float soulWeight = ConfigHelper.getFloatValue("nightfall.soul_weight", 0.5F);

        double ad = attacker.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);

        RegistryEntry.Reference<EntityAttribute> soulAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.SOUL);
        double soul = soulAttr != null ? attacker.getAttributeValue(soulAttr) : 0.0D;

        float adPart = adBaseline > 0.0F ? (float)(ad / adBaseline) : 1.0F;
        float soulPart = soulBaseline > 0.0F ? (float)(soul / soulBaseline) : 0.0F;

        float factor = adWeight * adPart + soulWeight * soulPart;
        return Math.max(0.0F, factor);
    }

    @Redirect(
            method = "onStoppedUsing",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;damage(Lnet/minecraft/entity/damage/DamageSource;F)Z"
            )
    )
    private boolean tmscompat$scaleNightfallAbilityDamage(Entity instance,
                                                          DamageSource source,
                                                          float originalAmount,
                                                          ItemStack stack,
                                                          World world,
                                                          LivingEntity user,
                                                          int remainingUseTicks) {
        float ability = (float) ConfigConstructor.nightfall_ability_damage;
        float scaledAbility = ability * tmscompat$scale(user);

        float ench = 0.0F;
        if (world instanceof ServerWorld serverWorld && instance instanceof LivingEntity target && user instanceof net.minecraft.entity.player.PlayerEntity player) {
            ench = EnchantmentHelper.getDamage(serverWorld, stack, target, world.getDamageSources().playerAttack(player), 0.0F);
        }

        float newAmount = scaledAbility + 2.0F * ench;
        return instance.damage(source, newAmount);
    }
}