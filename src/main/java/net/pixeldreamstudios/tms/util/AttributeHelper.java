package net.pixeldreamstudios.tms.util;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class AttributeHelper {

    @Nullable
    public static RegistryEntry.Reference<EntityAttribute> getAttributeEntry(Identifier id) {
        RegistryKey<EntityAttribute> key = RegistryKey.of(RegistryKeys.ATTRIBUTE, id);
        RegistryEntry.Reference<EntityAttribute> entry = Registries.ATTRIBUTE.getEntry(key).orElse(null);

        if (entry == null) {
            EntityAttribute attr = Registries.ATTRIBUTE.get(id);
            if (attr != null) {
                var optKey = Registries.ATTRIBUTE.getKey(attr);
                if (optKey.isPresent()) {
                    entry = Registries.ATTRIBUTE.getEntry(optKey.get()).orElse(null);
                }
            }
        }

        return entry;
    }

    private AttributeHelper() {}
}