package net.pixeldreamstudios.tms.mixin.soulsweapons.item_attribute;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.items.hammer.Mjolnir;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public abstract class MjolnirAttributeMixin {

    @Shadow
    public abstract net.minecraft.item.Item getItem();

    @Inject(
            method = "applyAttributeModifier(Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void tmscompat$addMjolnirAttributesSlot(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof Mjolnir) || slot != AttributeModifierSlot.MAINHAND) {
            return;
        }

        double lightningBonus = ConfigHelper.getDoubleValue("mjolnir.lightning_spell_power_bonus", 4.0);

        if (lightningBonus != 0.0) {
            RegistryEntry.Reference<EntityAttribute> lightningAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.LIGHTNING);

            if (lightningAttr != null) {
                EntityAttributeModifier lightningModifier = new EntityAttributeModifier(
                        TMSCompatIdentifiers.ModifierIds.MJOLNIR_LIGHTNING,
                        lightningBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                attributeModifierConsumer.accept(lightningAttr, lightningModifier);
            }
        }
    }

    @Inject(
            method = "applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void tmscompat$addMjolnirAttributesEquipment(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof Mjolnir) || slot != EquipmentSlot.MAINHAND) {
            return;
        }

        double lightningBonus = ConfigHelper.getDoubleValue("mjolnir.lightning_spell_power_bonus", 4.0);

        if (lightningBonus != 0.0) {
            RegistryEntry.Reference<EntityAttribute> lightningAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.LIGHTNING);

            if (lightningAttr != null) {
                EntityAttributeModifier lightningModifier = new EntityAttributeModifier(
                        TMSCompatIdentifiers.ModifierIds.MJOLNIR_LIGHTNING,
                        lightningBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                attributeModifierConsumer.accept(lightningAttr, lightningModifier);
            }
        }
    }
}