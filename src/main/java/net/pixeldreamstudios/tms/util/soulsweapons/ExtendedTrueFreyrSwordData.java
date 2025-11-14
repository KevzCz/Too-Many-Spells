package net.pixeldreamstudios.tms.util.soulsweapons;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;
import net.pixeldreamstudios.tms.spell.handler.TrueFreyrSwordDeliveryHandler;
import net.soulsweaponry.entitydata.IEntityDataSaver;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExtendedTrueFreyrSwordData {

    private static final String TRUE_SPELL_SUMMONS_KEY = "tms_true_spell_summons";
    public static final String SUMMON_TYPE = TrueFreyrSwordDeliveryHandler.TRUE_FREYR_SWORD_TYPE;

    public static boolean isSpellSummon(UUID entityUuid) {
        var data = SummonTracker.getSummonData(entityUuid);
        return data != null && SUMMON_TYPE.equals(data.summonType);
    }

    public static boolean isSpellSummon(LivingEntity entity, UUID swordUuid) {
        return getSpellSummonUuids(entity).contains(swordUuid);
    }

    public static int getSpellSummonCount(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            return SummonTracker.getPlayerSummonCountByType(player.getUuid(), SUMMON_TYPE);
        }
        return getSpellSummonUuids(entity).size();
    }

    public static UUID getOldestSpellSummon(LivingEntity entity) {
        if (entity instanceof PlayerEntity player) {
            return SummonTracker.getOldestSummonByType(player.getUuid(), SUMMON_TYPE);
        }
        List<UUID> summons = getSpellSummonUuids(entity);
        return summons.isEmpty() ? null : summons.get(0);
    }

    public static List<UUID> getSpellSummonUuids(LivingEntity entity) {
        NbtCompound nbt = ((IEntityDataSaver) entity).getPersistentData();
        List<UUID> uuids = new ArrayList<>();

        if (nbt.contains(TRUE_SPELL_SUMMONS_KEY, NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList(TRUE_SPELL_SUMMONS_KEY, NbtElement.INT_ARRAY_TYPE);
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

    public static void addSpellSummonUuid(LivingEntity entity, UUID swordUuid) {
        NbtCompound nbt = ((IEntityDataSaver) entity).getPersistentData();

        NbtList list;
        if (nbt.contains(TRUE_SPELL_SUMMONS_KEY, NbtElement.LIST_TYPE)) {
            list = nbt.getList(TRUE_SPELL_SUMMONS_KEY, NbtElement.INT_ARRAY_TYPE);
        } else {
            list = new NbtList();
        }

        list.add(new NbtIntArray(uuidToIntArray(swordUuid)));
        nbt.put(TRUE_SPELL_SUMMONS_KEY, list);
    }

    public static void removeSpellSummonUuid(LivingEntity entity, UUID swordUuid) {
        NbtCompound nbt = ((IEntityDataSaver) entity).getPersistentData();

        if (nbt.contains(TRUE_SPELL_SUMMONS_KEY, NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList(TRUE_SPELL_SUMMONS_KEY, NbtElement.INT_ARRAY_TYPE);
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

            nbt.put(TRUE_SPELL_SUMMONS_KEY, newList);
        }
    }

    public static void clearAllSpellSummons(LivingEntity entity) {
        NbtCompound nbt = ((IEntityDataSaver) entity).getPersistentData();
        nbt.remove(TRUE_SPELL_SUMMONS_KEY);
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