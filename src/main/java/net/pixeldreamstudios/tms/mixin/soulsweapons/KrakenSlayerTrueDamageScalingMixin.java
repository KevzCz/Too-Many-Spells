package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.soulsweaponry.items.bow.KrakenSlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Pseudo
@Mixin(KrakenSlayer.class)
public abstract class KrakenSlayerTrueDamageScalingMixin {
    @Unique
    private static final Identifier RANGED_DAMAGE_ID = Identifier.of("ranged_weapon", "damage");

    @ModifyVariable(
            method = "getKrakenSlayerProjectile",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true,
            remap = false
    )
    private static float tmscompat$scaleKrakenTrueDamage(float trueDamage, World world, ItemStack bowStack, LivingEntity shooter) {
        if (shooter == null) {
            return trueDamage;
        }

        RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, RANGED_DAMAGE_ID);
        RegistryEntry.Reference<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(key).orElse(null);
        if (entry == null) {
            EntityAttribute attr = Registries.ATTRIBUTE.get(RANGED_DAMAGE_ID);
            if (attr != null) {
                var optKey = Registries.ATTRIBUTE.getKey(attr);
                if (optKey.isPresent()) {
                    entry = Registries.ATTRIBUTE.getEntry(optKey.get()).orElse(null);
                }
            }
        }

        if (entry != null) {
            double rangedDamage = shooter.getAttributeValue(entry);
            ItemStack mainHandStack = shooter.getMainHandStack();
            double baseline = mainHandStack.getItem() instanceof net.soulsweaponry.items.crossbow.KrakenSlayerCrossbow ? 9.0 : 7.0;
            double bonus = Math.floor((rangedDamage - baseline) / 5.0);
            return Math.max(0.0F, (float)(trueDamage + bonus));
        }

        return trueDamage;
    }
}