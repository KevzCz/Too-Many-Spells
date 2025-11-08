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
import net.soulsweaponry.items.axe.LeviathanAxe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public abstract class LeviathanAxeAttributeMixin {

    @Shadow
    public abstract net.minecraft.item.Item getItem();

    @Inject(
            method = "applyAttributeModifier(Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void tmscompat$addLeviathanAxeAttributesSlot(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof LeviathanAxe) || slot != AttributeModifierSlot.MAINHAND) {
            return;
        }

        double frostBonus = ConfigHelper.getDoubleValue("leviathan_axe.frost_spell_power_bonus", 4.0);

        if (frostBonus != 0.0) {
            RegistryEntry.Reference<EntityAttribute> frostAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.FROST);

            if (frostAttr != null) {
                EntityAttributeModifier frostModifier = new EntityAttributeModifier(
                        TMSCompatIdentifiers.ModifierIds.LEVIATHAN_AXE_FROST,
                        frostBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                attributeModifierConsumer.accept(frostAttr, frostModifier);
            }
        }
    }

    @Inject(
            method = "applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void tmscompat$addLeviathanAxeAttributesEquipment(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof LeviathanAxe) || slot != EquipmentSlot.MAINHAND) {
            return;
        }

        double frostBonus = ConfigHelper.getDoubleValue("leviathan_axe.frost_spell_power_bonus", 4.0);

        if (frostBonus != 0.0) {
            RegistryEntry.Reference<EntityAttribute> frostAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.FROST);

            if (frostAttr != null) {
                EntityAttributeModifier frostModifier = new EntityAttributeModifier(
                        TMSCompatIdentifiers.ModifierIds.LEVIATHAN_AXE_FROST,
                        frostBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                attributeModifierConsumer.accept(frostAttr, frostModifier);
            }
        }
    }
}