package net.pixeldreamstudios.tms.spell.handler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.pixeldreamstudios.tms.TooManySpells;
import net.pixeldreamstudios.tms.util.ExtendedFreyrSwordData;
import net.pixeldreamstudios.tms.util.SummonTracker;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellHelper;
import net.soulsweaponry.entity.mobs.FreyrSwordEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class FreyrSwordDeliveryHandler implements SpellHandlers.CustomDelivery {

    private static final int LIFETIME_TICKS = 600;
    private static final int DEFAULT_MAX_SUMMONS = 3;
    private static final boolean ALLOW_INTERACTION = false;

    @Override
    public boolean onSpellDelivery(
            World world,
            RegistryEntry<Spell> spellEntry,
            PlayerEntity caster,
            List<SpellHelper.DeliveryTarget> targets,
            SpellHelper.ImpactContext context,
            @Nullable Vec3d customLocation) {

        if (!(world instanceof ServerWorld serverWorld)) {
            return false;
        }

        TooManySpells.LOGGER.info("=== FREYR SWORD SPELL CAST ===");
        TooManySpells.LOGGER.info("Caster: {}", caster.getName().getString());

        Spell spell = spellEntry.value();

        if (spell.impacts == null || spell.impacts.isEmpty()) {
            return false;
        }

        int maxSummons = getMaxSummons(caster);
        int currentCount = SummonTracker.getPlayerSummonCountByType(caster.getUuid(), ExtendedFreyrSwordData.SUMMON_TYPE);
        TooManySpells.LOGGER.info("Current Freyr Sword spell summons: {} / {}", currentCount, maxSummons);

        if (currentCount >= maxSummons) {
            UUID oldestUuid = SummonTracker.getOldestSummonByType(caster.getUuid(), ExtendedFreyrSwordData.SUMMON_TYPE);
            if (oldestUuid != null) {
                SummonTracker.SummonData oldData = SummonTracker.getSummonData(oldestUuid);
                if (oldData != null) {
                    Entity oldEntity = oldData.getEntity();
                    if (oldEntity != null) {
                        TooManySpells.LOGGER.info("Removing oldest Freyr Sword summon (limit reached)");
                        oldEntity.discard();
                    }
                }
                ExtendedFreyrSwordData.unregisterSpellSummon(caster, oldestUuid);
            }
        }

        for (Spell.Impact impact : spell.impacts) {
            if (impact.action != null && impact.action.spawns != null) {
                for (Spell.Impact.Action.Spawn spawnData : impact.action.spawns) {
                    spawnFreyrSword(serverWorld, caster, spawnData);
                }
            }
        }

        return true;
    }

    private void spawnFreyrSword(ServerWorld world, PlayerEntity player, Spell.Impact.Action.Spawn spawnData) {
        Identifier itemId = Identifier.of("soulsweapons", "freyr_sword");
        var item = Registries.ITEM.get(itemId);
        ItemStack stack = new ItemStack(item);

        FreyrSwordEntity freyrSword = new FreyrSwordEntity(world, player, stack);

        Vec3d spawnPos = calculateSpawnPosition(player, spawnData.placement);
        freyrSword.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        freyrSword.setYaw(player.getYaw());
        freyrSword.setStationaryPos(FreyrSwordEntity.NULLISH_POS);

        boolean spawned = world.spawnEntity(freyrSword);

        if (!spawned) {
            TooManySpells.LOGGER.error("Failed to spawn Freyr Sword entity!");
            return;
        }

        UUID entityUuid = freyrSword.getUuid();
        TooManySpells.LOGGER.info("Spawned Freyr Sword entity: {}", entityUuid);

        ExtendedFreyrSwordData.registerSpellSummon(player, freyrSword, world, LIFETIME_TICKS, ALLOW_INTERACTION);
        TooManySpells.LOGGER.info("Registered Freyr Sword spell summon: {}", entityUuid);

        player.playSound(SoundEvents.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1.0F, 1.0F);
    }

    private Vec3d calculateSpawnPosition(PlayerEntity player, Spell.EntityPlacement placement) {
        Vec3d playerPos = player.getPos();
        Vec3d lookVec = player.getRotationVector();

        double x = playerPos.x + lookVec.x * placement.location_offset_by_look;
        double y = playerPos.y;
        double z = playerPos.z + lookVec.z * placement.location_offset_by_look;

        x += placement.location_offset_x;
        y += placement.location_offset_y;
        z += placement.location_offset_z;

        return new Vec3d(x, y, z);
    }

    private int getMaxSummons(PlayerEntity player) {
        return DEFAULT_MAX_SUMMONS;
    }
}