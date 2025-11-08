package net.pixeldreamstudios.tms.mixin.soulsweapons.item_attribute;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.config.TMSSoulsweaponsConfig;
import net.pixeldreamstudios.tms.util.AttributeHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.items.sword.Frostmourne;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public abstract class FrostmourneAttributeMixin {

    @Shadow
    public abstract net.minecraft.item.Item getItem();

    @Inject(
            method = "applyAttributeModifier(Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void tmscompat$addFrostmourneAttributesSlot(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof Frostmourne) || slot != AttributeModifierSlot.MAINHAND) {
            return;
        }

        double frostBonus = ConfigHelper.getDoubleValue("frostmourne.frost_spell_power_bonus", 3.0);

        if (frostBonus != 0.0) {
            RegistryEntry.Reference<EntityAttribute> frostAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.FROST);
            if (frostAttr != null) {
                EntityAttributeModifier frostModifier = new EntityAttributeModifier(
                        TMSCompatIdentifiers.ModifierIds.FROSTMOURNE_FROST,
                        frostBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                attributeModifierConsumer.accept(frostAttr, frostModifier);
            }
        }

        if (TMSSoulsweaponsConfig.getInstance().frostmourne.useKevslibraryPetInheritanceAttribute) {
            double petInheritanceBonus = ConfigHelper.getDoubleValue("frostmourne.pet_inheritance_bonus", 0.075);
            if (petInheritanceBonus != 0.0) {
                RegistryEntry.Reference<EntityAttribute> petAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.KevsLibrary.PET_INHERITANCE_RATIO);
                if (petAttr != null) {
                    EntityAttributeModifier petModifier = new EntityAttributeModifier(
                            TMSCompatIdentifiers.ModifierIds.FROSTMOURNE_PET_INHERITANCE,
                            petInheritanceBonus,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    );
                    attributeModifierConsumer.accept(petAttr, petModifier);
                }
            }
        }
    }

    @Inject(
            method = "applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void tmscompat$addFrostmourneAttributesEquipment(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof Frostmourne) || slot != EquipmentSlot.MAINHAND) {
            return;
        }

        double frostBonus = ConfigHelper.getDoubleValue("frostmourne.frost_spell_power_bonus", 3.0);

        if (frostBonus != 0.0) {
            RegistryEntry.Reference<EntityAttribute> frostAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.FROST);
            if (frostAttr != null) {
                EntityAttributeModifier frostModifier = new EntityAttributeModifier(
                        TMSCompatIdentifiers.ModifierIds.FROSTMOURNE_FROST,
                        frostBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                attributeModifierConsumer.accept(frostAttr, frostModifier);
            }
        }

        if (TMSSoulsweaponsConfig.getInstance().frostmourne.useKevslibraryPetInheritanceAttribute) {
            double petInheritanceBonus = ConfigHelper.getDoubleValue("frostmourne.pet_inheritance_bonus", 0.075);
            if (petInheritanceBonus != 0.0) {
                RegistryEntry.Reference<EntityAttribute> petAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.KevsLibrary.PET_INHERITANCE_RATIO);
                if (petAttr != null) {
                    EntityAttributeModifier petModifier = new EntityAttributeModifier(
                            TMSCompatIdentifiers.ModifierIds.FROSTMOURNE_PET_INHERITANCE,
                            petInheritanceBonus,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    );
                    attributeModifierConsumer.accept(petAttr, petModifier);
                }
            }
        }
    }
}