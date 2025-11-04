package net.pixeldreamstudios.tms.spell.handler;

import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.pixeldreamstudios.summonerlib.attribute.SummonerAttributes;
import net.pixeldreamstudios.summonerlib.data.SummonData;
import net.pixeldreamstudios.summonerlib.manager.SummonManager;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;
import net.pixeldreamstudios.tms.util.ExtendedFreyrSwordData;
import net.soulsweaponry.entity.mobs.FreyrSwordEntity;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class FreyrSwordDeliveryHandler implements SpellHandlers.CustomDelivery {

    private static final int BASE_LIFETIME_TICKS = 600;
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

        Spell spell = spellEntry.value();

        if (spell.impacts == null || spell.impacts.isEmpty()) {
            return false;
        }

        int maxSummons = SummonManager.getMaxSummons(caster);
        int currentCount = SummonTracker.getPlayerSummonCountByType(caster.getUuid(), ExtendedFreyrSwordData.SUMMON_TYPE);

        if (currentCount >= maxSummons) {
            UUID oldestUuid = SummonTracker.getOldestSummonByType(caster.getUuid(), ExtendedFreyrSwordData.SUMMON_TYPE);
            if (oldestUuid != null) {
                SummonData oldData = SummonTracker.getSummonData(oldestUuid);
                if (oldData != null) {
                    Entity oldEntity = oldData.getEntity();
                    if (oldEntity != null) {
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

        if (freyrSword.getAttributeInstance(EntityAttributes.GENERIC_SCALE) != null) {
            freyrSword.getAttributeInstance(EntityAttributes.GENERIC_SCALE).setBaseValue(0.75);
        }

        double durationMultiplier = player.getAttributeValue(SummonerAttributes.SUMMON_DURATION);
        int lifetime = (int) (BASE_LIFETIME_TICKS * durationMultiplier);

        double damageMultiplier = player.getAttributeValue(SummonerAttributes.SUMMON_DAMAGE);
        double healthMultiplier = player.getAttributeValue(SummonerAttributes.SUMMON_HEALTH);

        if (freyrSword.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE) != null) {
            double baseDamage = freyrSword.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            freyrSword.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE)
                    .setBaseValue(baseDamage * damageMultiplier);
        }

        if (freyrSword.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH) != null) {
            double baseHealth = freyrSword.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
            freyrSword.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)
                    .setBaseValue(baseHealth * healthMultiplier);
            freyrSword.setHealth(freyrSword.getMaxHealth());
        }

        boolean spawned = world.spawnEntity(freyrSword);

        if (!spawned) {
            return;
        }

        ExtendedFreyrSwordData.registerSpellSummon(player, freyrSword, world, lifetime, ALLOW_INTERACTION);

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
}