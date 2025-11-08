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
import net.soulsweaponry.items.sword.FreyrSword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public abstract class FreyrSwordAttributeMixin {

    @Shadow
    public abstract net.minecraft.item.Item getItem();

    @Inject(
            method = "applyAttributeModifier(Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void tmscompat$addFreyrSwordAttributesSlot(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof FreyrSword) || slot != AttributeModifierSlot.MAINHAND) {
            return;
        }

        double soulSpellPowerBonus = ConfigHelper.getDoubleValue("freyr_sword.soul_spell_power_bonus", 4.0);
        if (soulSpellPowerBonus != 0.0) {
            RegistryEntry.Reference<EntityAttribute> soulAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.SOUL);
            if (soulAttr != null) {
                EntityAttributeModifier soulModifier = new EntityAttributeModifier(
                        TMSCompatIdentifiers.ModifierIds.FREYR_SWORD_SOUL,
                        soulSpellPowerBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                attributeModifierConsumer.accept(soulAttr, soulModifier);
            }
        }

        if (TMSSoulsweaponsConfig.getInstance().freyr_sword.useKevslibraryPetInheritanceAttribute) {
            double petInheritanceBonus = ConfigHelper.getDoubleValue("freyr_sword.pet_inheritance_bonus", 0.2);
            if (petInheritanceBonus != 0.0) {
                RegistryEntry.Reference<EntityAttribute> petAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.KevsLibrary.PET_INHERITANCE_RATIO);
                if (petAttr != null) {
                    EntityAttributeModifier petModifier = new EntityAttributeModifier(
                            TMSCompatIdentifiers.ModifierIds.FREYR_SWORD_PET_INHERITANCE,
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
    private void tmscompat$addFreyrSwordAttributesEquipment(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof FreyrSword) || slot != EquipmentSlot.MAINHAND) {
            return;
        }

        double soulSpellPowerBonus = ConfigHelper.getDoubleValue("freyr_sword.soul_spell_power_bonus", 4.0);
        if (soulSpellPowerBonus != 0.0) {
            RegistryEntry.Reference<EntityAttribute> soulAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.SOUL);
            if (soulAttr != null) {
                EntityAttributeModifier soulModifier = new EntityAttributeModifier(
                        TMSCompatIdentifiers.ModifierIds.FREYR_SWORD_SOUL,
                        soulSpellPowerBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                attributeModifierConsumer.accept(soulAttr, soulModifier);
            }
        }

        if (TMSSoulsweaponsConfig.getInstance().freyr_sword.useKevslibraryPetInheritanceAttribute) {
            double petInheritanceBonus = ConfigHelper.getDoubleValue("freyr_sword.pet_inheritance_bonus", 0.2);
            if (petInheritanceBonus != 0.0) {
                RegistryEntry.Reference<EntityAttribute> petAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.KevsLibrary.PET_INHERITANCE_RATIO);
                if (petAttr != null) {
                    EntityAttributeModifier petModifier = new EntityAttributeModifier(
                            TMSCompatIdentifiers.ModifierIds.FREYR_SWORD_PET_INHERITANCE,
                            petInheritanceBonus,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    );
                    attributeModifierConsumer.accept(petAttr, petModifier);
                }
            }
        }
    }
}