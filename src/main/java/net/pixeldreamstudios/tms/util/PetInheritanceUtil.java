package net.pixeldreamstudios.tms.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public class PetInheritanceUtil {

    public static Set<RegistryEntry<EntityAttribute>> getInheritableAttributes() {
        Set<RegistryEntry<EntityAttribute>> set = new HashSet<>();

        set.add(EntityAttributes.GENERIC_MAX_HEALTH);
        set.add(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        set.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE);
        set.add(EntityAttributes.GENERIC_ARMOR);
        set.add(EntityAttributes.GENERIC_MOVEMENT_SPEED);

        addIfPresent(set, "spell_power", "fire");
        addIfPresent(set, "spell_power", "frost");
        addIfPresent(set, "spell_power", "arcane");
        addIfPresent(set, "spell_power", "air");
        addIfPresent(set, "spell_power", "earth");
        addIfPresent(set, "spell_power", "water");
        addIfPresent(set, "spell_power", "lightning");
        addIfPresent(set, "spell_power", "soul");
        addIfPresent(set, "spell_power", "healing");

        return set;
    }

    private static void addIfPresent(Set<RegistryEntry<EntityAttribute>> set, String namespace, String path) {
        Registries.ATTRIBUTE.getEntry(Identifier.of(namespace, path)).ifPresent(set::add);
    }

    public static NbtCompound apply(PlayerEntity owner, LivingEntity pet, NbtCompound previousData, double ratio) {
        boolean isReapplying = previousData != null && !previousData.isEmpty();
        NbtCompound baseAttrTag = isReapplying && previousData.contains("petBaseAttributes")
                ? previousData.getCompound("petBaseAttributes")
                : new NbtCompound();

        NbtCompound newInheritance = new NbtCompound();
        NbtCompound newBaseAttrTag = new NbtCompound();

        for (RegistryEntry<EntityAttribute> attribute : getInheritableAttributes()) {
            String key = attribute.getKey().map(id -> id.getValue().toString()).orElse(null);
            if (key == null) continue;

            EntityAttributeInstance ownerAttr = owner.getAttributeInstance(attribute);
            EntityAttributeInstance petAttr = pet.getAttributeInstance(attribute);
            if (ownerAttr == null || petAttr == null) continue;

            double baseValue;
            if (isReapplying && baseAttrTag.contains(key)) {
                baseValue = baseAttrTag.getDouble(key);
            } else {
                baseValue = petAttr.getBaseValue();
                newBaseAttrTag.putDouble(key, baseValue);
            }

            double bonus = ownerAttr.getValue() * ratio;

            if (attribute.value().equals(EntityAttributes.GENERIC_MOVEMENT_SPEED)) {
                bonus = Math.min(bonus, 0.2);
            }

            petAttr.setBaseValue(baseValue + bonus);
            newInheritance.putDouble(key, bonus);

            if (attribute.value().equals(EntityAttributes.GENERIC_MAX_HEALTH)) {
                pet.setHealth((float) pet.getAttributeValue(attribute));
            }
        }

        if (!newBaseAttrTag.isEmpty()) {
            newInheritance.put("petBaseAttributes", newBaseAttrTag);
        }
        newInheritance.putBoolean("petAttributesInherited", true);
        return newInheritance;
    }
}