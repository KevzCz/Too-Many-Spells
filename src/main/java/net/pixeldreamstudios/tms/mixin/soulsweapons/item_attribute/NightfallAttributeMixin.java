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
import net.soulsweaponry.items.hammer.Nightfall;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public abstract class NightfallAttributeMixin {

    @Shadow
    public abstract net.minecraft.item.Item getItem();

    @Inject(
            method = "applyAttributeModifier(Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void tmscompat$addNightfallAttributesSlot(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof Nightfall) || slot != AttributeModifierSlot.MAINHAND) {
            return;
        }

        double soulBonus = ConfigHelper.getDoubleValue("nightfall.soul_spell_power_bonus", 3.0);

        if (soulBonus != 0.0) {
            RegistryEntry.Reference<EntityAttribute> soulAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.SOUL);

            if (soulAttr != null) {
                EntityAttributeModifier soulModifier = new EntityAttributeModifier(
                        TMSCompatIdentifiers.ModifierIds.NIGHTFALL_SOUL,
                        soulBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                attributeModifierConsumer.accept(soulAttr, soulModifier);
            }
        }

        if (TMSSoulsweaponsConfig.getInstance().nightfall.useKevslibraryPetInheritanceAttribute) {
            double petInheritanceBonus = ConfigHelper.getDoubleValue("nightfall.pet_inheritance_bonus", 0.1);
            if (petInheritanceBonus != 0.0) {
                RegistryEntry.Reference<EntityAttribute> petAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.KevsLibrary.PET_INHERITANCE_RATIO);

                if (petAttr != null) {
                    EntityAttributeModifier petModifier = new EntityAttributeModifier(
                            TMSCompatIdentifiers.ModifierIds.NIGHTFALL_PET_INHERITANCE,
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
    private void tmscompat$addNightfallAttributesEquipment(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof Nightfall) || slot != EquipmentSlot.MAINHAND) {
            return;
        }

        double soulBonus = ConfigHelper.getDoubleValue("nightfall.soul_spell_power_bonus", 3.0);

        if (soulBonus != 0.0) {
            RegistryEntry.Reference<EntityAttribute> soulAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.SpellPower.SOUL);

            if (soulAttr != null) {
                EntityAttributeModifier soulModifier = new EntityAttributeModifier(
                        TMSCompatIdentifiers.ModifierIds.NIGHTFALL_SOUL,
                        soulBonus,
                        EntityAttributeModifier.Operation.ADD_VALUE
                );
                attributeModifierConsumer.accept(soulAttr, soulModifier);
            }
        }

        if (TMSSoulsweaponsConfig.getInstance().nightfall.useKevslibraryPetInheritanceAttribute) {
            double petInheritanceBonus = ConfigHelper.getDoubleValue("nightfall.pet_inheritance_bonus", 0.1);
            if (petInheritanceBonus != 0.0) {
                RegistryEntry.Reference<EntityAttribute> petAttr = AttributeHelper.getAttributeEntry(TMSCompatIdentifiers.KevsLibrary.PET_INHERITANCE_RATIO);

                if (petAttr != null) {
                    EntityAttributeModifier petModifier = new EntityAttributeModifier(
                            TMSCompatIdentifiers.ModifierIds.NIGHTFALL_PET_INHERITANCE,
                            petInheritanceBonus,
                            EntityAttributeModifier.Operation.ADD_VALUE
                    );
                    attributeModifierConsumer.accept(petAttr, petModifier);
                }
            }
        }
    }
}