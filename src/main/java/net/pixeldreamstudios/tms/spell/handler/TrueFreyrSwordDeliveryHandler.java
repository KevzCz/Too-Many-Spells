package net.pixeldreamstudios.tms.spell.handler;

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
import net.pixeldreamstudios.summonerlib.api.SummonBuilder;
import net.pixeldreamstudios.summonerlib.manager.SummonLifecycleManager;
import net.pixeldreamstudios.summonerlib.util.SummonAttributeApplicator;
import net.pixeldreamstudios.summonerlib.util.SummonLimitHandler;
import net.pixeldreamstudios.summonerlib.util.SummonMergeUtil;
import net.pixeldreamstudios.tms.entity.soulsweapons.TrueFreyrSwordEntity;
import net.pixeldreamstudios.tms.util.soulsweapons.ExtendedFreyrSwordData;
import net.soulsweaponry.entity.mobs.FreyrSwordEntity;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellHelper;
import net.spell_power.api.SpellSchools;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TrueFreyrSwordDeliveryHandler implements SpellHandlers.CustomDelivery {

    private static final boolean ALLOW_INTERACTION = false;
    public static final int SLOT_COST = 2;
    private static final double BASE_SCALE = 1.5;
    private static final float COEFFICIENT_MULTIPLIER = 2.0F;
    private static final double MERGE_STAT_BONUS = 0.35;
    public static final String TRUE_FREYR_SWORD_TYPE = "too-many-spells:true_freyr_sword";

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

        SummonLimitHandler.handleSummonLimit(
                caster,
                TRUE_FREYR_SWORD_TYPE,
                SLOT_COST
        );

        for (Spell.Impact impact : spell.impacts) {
            if (impact.action != null && impact.action.spawns != null) {
                for (Spell.Impact.Action.Spawn spawnData : impact.action.spawns) {
                    spawnTrueFreyrSword(serverWorld, caster, spawnData, impact);
                }
            }
        }

        return true;
    }

    private void spawnTrueFreyrSword(ServerWorld world, PlayerEntity player, Spell.Impact.Action.Spawn spawnData, Spell.Impact impact) {
        Identifier itemId = Identifier.of("soulsweapons", "freyr_sword");
        var item = Registries.ITEM.get(itemId);
        ItemStack stack = new ItemStack(item);

        TrueFreyrSwordEntity trueFreyrSword = new TrueFreyrSwordEntity(world, player, stack);

        Vec3d spawnPos = calculateSpawnPosition(player, spawnData.placement);
        trueFreyrSword.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        trueFreyrSword.setYaw(player.getYaw());
        trueFreyrSword.setStationaryPos(FreyrSwordEntity.NULLISH_POS);

        if (trueFreyrSword.getAttributeInstance(EntityAttributes.GENERIC_SCALE) != null) {
            trueFreyrSword.getAttributeInstance(EntityAttributes.GENERIC_SCALE).setBaseValue(BASE_SCALE);
        }

        float baseCoefficient = 0.13F;
        if (impact.action != null && impact.action.damage != null) {
            baseCoefficient = impact.action.damage.spell_power_coefficient;
        }
        float coefficient = baseCoefficient;

        SummonAttributeApplicator.AttributeConfig config = new SummonAttributeApplicator.AttributeConfig(
                player,
                trueFreyrSword,
                coefficient,
                SpellSchools.SOUL
        );

        SummonAttributeApplicator.applyAllAttributes(config);

        TrueFreyrSwordEntity spawned = SummonBuilder.create(player, trueFreyrSword, world)
                .withType(TRUE_FREYR_SWORD_TYPE)
                .withLifetime(-1)
                .allowInteraction(ALLOW_INTERACTION)
                .slotCost(SLOT_COST)
                .group("true_freyr_swords")
                .onSpawn(sword -> {
                    ExtendedFreyrSwordData.addSpellSummonUuid(player, sword.getUuid());
                    SummonLifecycleManager.spawnSummonParticles(world, sword);
                })
                .build();

        if (spawned != null) {
            mergeAllTrueFreyrSwords(player, world);

            player.playSound(SoundEvents.ENTITY_WITHER_SPAWN, 1.0F, 1.5F);
        }
    }

    private void mergeAllTrueFreyrSwords(PlayerEntity player, ServerWorld world) {
        TrueFreyrSwordEntity merged = SummonMergeUtil.mergeAllIntoOldest(
                player,
                TRUE_FREYR_SWORD_TYPE,
                world,
                this::applyMergeBonus
        );
    }

    private void applyMergeBonus(TrueFreyrSwordEntity sword, int totalSlots) {
        if (totalSlots <= SLOT_COST) {
            sword.setMergeCount(0);
            return;
        }

        int mergeCount = (totalSlots / SLOT_COST) - 1;
        sword.setMergeCount(mergeCount);

        double multiplier = 1.0 + (mergeCount * MERGE_STAT_BONUS);

        if (sword.getAttributeInstance(EntityAttributes.GENERIC_SCALE) != null) {
            double newScale = BASE_SCALE * multiplier;
            sword.getAttributeInstance(EntityAttributes.GENERIC_SCALE).setBaseValue(newScale);
        }

        if (sword.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE) != null) {
            double currentDamage = sword.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            double baseDamage = currentDamage / (1.0 + ((mergeCount - 1) * MERGE_STAT_BONUS));
            double newDamage = baseDamage * multiplier;
            sword.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(newDamage);
        }

        if (sword.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH) != null) {
            double currentHealth = sword.getAttributeValue(EntityAttributes.GENERIC_MAX_HEALTH);
            double baseHealth = currentHealth / (1.0 + ((mergeCount - 1) * MERGE_STAT_BONUS));
            double newHealth = baseHealth * multiplier;
            sword.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(newHealth);
            sword.setHealth((float) newHealth);
        }

        if (sword.getAttributeInstance(EntityAttributes.GENERIC_ARMOR) != null) {
            double currentArmor = sword.getAttributeValue(EntityAttributes.GENERIC_ARMOR);
            double baseArmor = currentArmor / (1.0 + ((mergeCount - 1) * MERGE_STAT_BONUS));
            double newArmor = baseArmor * multiplier;
            sword.getAttributeInstance(EntityAttributes.GENERIC_ARMOR).setBaseValue(newArmor);
        }

        ServerWorld world = (ServerWorld) sword.getWorld();

        world.spawnParticles(
                net.minecraft.particle.ParticleTypes.SOUL_FIRE_FLAME,
                sword.getX(),
                sword.getY() + 1.0,
                sword.getZ(),
                30 + (mergeCount * 20),
                0.5, 0.8, 0.5,
                0.15
        );

        world.spawnParticles(
                net.minecraft.particle.ParticleTypes.END_ROD,
                sword.getX(),
                sword.getY() + 0.5,
                sword.getZ(),
                20 + (mergeCount * 10),
                0.3, 0.5, 0.3,
                0.1
        );

        world.playSound(
                null,
                sword.getX(), sword.getY(), sword.getZ(),
                SoundEvents.BLOCK_BEACON_POWER_SELECT,
                sword.getSoundCategory(),
                1.0F + (mergeCount * 0.2F),
                1.5F + (mergeCount * 0.1F)
        );
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