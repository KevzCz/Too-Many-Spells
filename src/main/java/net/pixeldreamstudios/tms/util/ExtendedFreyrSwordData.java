package net.pixeldreamstudios.tms.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.pixeldreamstudios.tms.TooManySpells;
import net.soulsweaponry.entity.mobs.FreyrSwordEntity;
import net.soulsweaponry.entitydata.IEntityDataSaver;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExtendedFreyrSwordData {

    private static final String SPELL_SUMMONS_KEY = "tms_spell_summons";
    private static final String ORIGINAL_SUMMON_KEY = "freyr_sword_summon_uuid";
    public static final String SUMMON_TYPE = "freyr_sword";

    public static void registerSpellSummon(PlayerEntity owner, FreyrSwordEntity entity, ServerWorld world, int lifetimeTicks, boolean allowInteraction) {
        UUID entityUuid = entity.getUuid();

        addSpellSummonUuid(owner, entityUuid);

        SummonTracker.registerSummon(owner, entity, world.getTime(), lifetimeTicks, allowInteraction, SUMMON_TYPE);

        TooManySpells.LOGGER.info("Fully registered Freyr Sword spell summon: {}", entityUuid);
    }

    public static void unregisterSpellSummon(PlayerEntity owner, UUID entityUuid) {
        removeSpellSummonUuid(owner, entityUuid);

        if (!owner.getWorld().isClient() && owner.getWorld() instanceof ServerWorld serverWorld) {
            SummonTracker.unregisterSummon(serverWorld, entityUuid);
        } else {
            SummonTracker.unregisterSummon(entityUuid);
        }

        TooManySpells.LOGGER.info("Fully unregistered Freyr Sword spell summon: {}", entityUuid);
    }

    public static boolean isSpellSummon(UUID entityUuid) {
        SummonTracker.SummonData data = SummonTracker.getSummonData(entityUuid);
        return data != null && SUMMON_TYPE.equals(data.summonType);
    }

    public static boolean isSpellSummon(LivingEntity entity, UUID swordUuid) {
        return getSpellSummonUuids(entity).contains(swordUuid);
    }

    public static boolean isOriginalSummon(LivingEntity entity, UUID swordUuid) {
        NbtCompound nbt = ((IEntityDataSaver) entity).getPersistentData();
        if (!nbt.contains(ORIGINAL_SUMMON_KEY)) {
            return false;
        }
        UUID originalUuid = nbt.getUuid(ORIGINAL_SUMMON_KEY);
        return originalUuid.equals(swordUuid);
    }

    public static boolean isOwnedSummon(LivingEntity entity, UUID swordUuid) {
        return isOriginalSummon(entity, swordUuid) || isSpellSummon(entity, swordUuid);
    }

    public static int getSpellSummonCount(LivingEntity entity) {
        return getSpellSummonUuids(entity).size();
    }

    public static UUID getOldestSpellSummon(LivingEntity entity) {
        List<UUID> summons = getSpellSummonUuids(entity);
        return summons.isEmpty() ? null : summons.get(0);
    }


    private static List<UUID> getSpellSummonUuids(LivingEntity entity) {
        NbtCompound nbt = ((IEntityDataSaver) entity).getPersistentData();
        List<UUID> uuids = new ArrayList<>();

        if (nbt.contains(SPELL_SUMMONS_KEY, NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList(SPELL_SUMMONS_KEY, NbtElement.INT_ARRAY_TYPE);
            for (int i = 0; i < list.size(); i++) {
                int[] uuidArray = list.getIntArray(i);
                if (uuidArray.length == 4) {
                    UUID uuid = uuidFromIntArray(uuidArray);
                    uuids.add(uuid);
                }
            }
        }

        return uuids;
    }

    private static void addSpellSummonUuid(LivingEntity entity, UUID swordUuid) {
        NbtCompound nbt = ((IEntityDataSaver) entity).getPersistentData();

        NbtList list;
        if (nbt.contains(SPELL_SUMMONS_KEY, NbtElement.LIST_TYPE)) {
            list = nbt.getList(SPELL_SUMMONS_KEY, NbtElement.INT_ARRAY_TYPE);
        } else {
            list = new NbtList();
        }

        list.add(new NbtIntArray(uuidToIntArray(swordUuid)));
        nbt.put(SPELL_SUMMONS_KEY, list);

        TooManySpells.LOGGER.debug("Added {} to player NBT", swordUuid);
    }

    public static void removeSpellSummonUuid(LivingEntity entity, UUID swordUuid) {
        NbtCompound nbt = ((IEntityDataSaver) entity).getPersistentData();

        if (nbt.contains(SPELL_SUMMONS_KEY, NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList(SPELL_SUMMONS_KEY, NbtElement.INT_ARRAY_TYPE);
            NbtList newList = new NbtList();

            for (int i = 0; i < list.size(); i++) {
                int[] uuidArray = list.getIntArray(i);
                if (uuidArray.length == 4) {
                    UUID uuid = uuidFromIntArray(uuidArray);
                    if (!uuid.equals(swordUuid)) {
                        newList.add(new NbtIntArray(uuidArray));
                    }
                }
            }

            nbt.put(SPELL_SUMMONS_KEY, newList);
            TooManySpells.LOGGER.debug("Removed {} from player NBT", swordUuid);
        }
    }

    public static void clearAllSpellSummons(LivingEntity entity) {
        NbtCompound nbt = ((IEntityDataSaver) entity).getPersistentData();
        nbt.remove(SPELL_SUMMONS_KEY);
    }


    private static int[] uuidToIntArray(UUID uuid) {
        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();

        return new int[]{
                (int)(mostSigBits >> 32),
                (int)mostSigBits,
                (int)(leastSigBits >> 32),
                (int)leastSigBits
        };
    }

    private static UUID uuidFromIntArray(int[] array) {
        long mostSigBits = (long)array[0] << 32 | (long)array[1] & 0xFFFFFFFFL;
        long leastSigBits = (long)array[2] << 32 | (long)array[3] & 0xFFFFFFFFL;
        return new UUID(mostSigBits, leastSigBits);
    }
}