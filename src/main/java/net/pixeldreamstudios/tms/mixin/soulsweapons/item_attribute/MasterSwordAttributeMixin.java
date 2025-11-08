package net.pixeldreamstudios.tms.mixin.soulsweapons.item_attribute;

import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.pixeldreamstudios.tms.util.TMSCompatIdentifiers;
import net.soulsweaponry.items.sword.MasterSword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Mixin(ItemStack.class)
public abstract class MasterSwordAttributeMixin {

    @Shadow
    public abstract net.minecraft.item.Item getItem();

    @Inject(
            method = "applyAttributeModifier(Lnet/minecraft/component/type/AttributeModifierSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void tmscompat$addMasterSwordAttributesSlot(AttributeModifierSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof MasterSword) || slot != AttributeModifierSlot.MAINHAND) {
            return;
        }

        double healthBonus = ConfigHelper.getDoubleValue("master_sword.max_health_bonus_percentage", 0.20);

        if (healthBonus != 0.0) {
            EntityAttributeModifier healthModifier = new EntityAttributeModifier(
                    TMSCompatIdentifiers.ModifierIds.MASTER_SWORD_HEALTH,
                    healthBonus,
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
            );
            attributeModifierConsumer.accept(EntityAttributes.GENERIC_MAX_HEALTH, healthModifier);
        }
    }

    @Inject(
            method = "applyAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V",
            at = @At("RETURN")
    )
    private void tmscompat$addMasterSwordAttributesEquipment(EquipmentSlot slot, BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer, CallbackInfo ci) {
        if (!(this.getItem() instanceof MasterSword) || slot != EquipmentSlot.MAINHAND) {
            return;
        }

        double healthBonus = ConfigHelper.getDoubleValue("master_sword.max_health_bonus_percentage", 0.20);

        if (healthBonus != 0.0) {
            EntityAttributeModifier healthModifier = new EntityAttributeModifier(
                    TMSCompatIdentifiers.ModifierIds.MASTER_SWORD_HEALTH,
                    healthBonus,
                    EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
            );
            attributeModifierConsumer.accept(EntityAttributes.GENERIC_MAX_HEALTH, healthModifier);
        }
    }
}