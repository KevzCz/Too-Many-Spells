package net.pixeldreamstudios.tms.entity.soulsweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.pixeldreamstudios.summonerlib.api.ISummonable;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;
import net.pixeldreamstudios.tms.effect.FatedVictoryEffect;
import net.pixeldreamstudios.tms.registry.TMSStatusEffects;
import net.pixeldreamstudios.tms.spell.handler.TrueFreyrSwordDeliveryHandler;
import net.soulsweaponry.entity.mobs.FreyrSwordEntity;
import net.spell_engine.internals.target.EntityRelation;
import net.spell_engine.internals.target.EntityRelations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TrueFreyrSwordEntity extends FreyrSwordEntity implements ISummonable {

    private static final Map<UUID, Long> HIT_COOLDOWNS = new HashMap<>();
    private static final long COOLDOWN_TICKS = 200;
    private static final int EFFECT_DURATION = 300;
    private static final double BASE_AOE_RADIUS = 8.0;

    private int mergeCount = 0;

    public TrueFreyrSwordEntity(net.minecraft.entity.EntityType<? extends FreyrSwordEntity> entityType, net.minecraft.world.World world) {
        super(entityType, world);
    }

    public TrueFreyrSwordEntity(net.minecraft.world.World world, PlayerEntity owner, ItemStack stack) {
        super(world, owner, stack);
    }

    public void setMergeCount(int count) {
        this.mergeCount = count;
    }

    public int getMergeCount() {
        return this.mergeCount;
    }

    private double getScaledAOERadius() {
        double scale = 1.0;
        if (this.getAttributeInstance(EntityAttributes.GENERIC_SCALE) != null) {
            scale = this.getAttributeValue(EntityAttributes.GENERIC_SCALE);
        }
        return BASE_AOE_RADIUS * scale;
    }

    @Override
    public void onSummonHit(LivingEntity target, float damage, boolean wasCritical) {

        if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }

        long currentTime = serverWorld.getTime();
        Long lastHitTime = HIT_COOLDOWNS.get(this.getUuid());

        if (lastHitTime != null && (currentTime - lastHitTime) < COOLDOWN_TICKS) {
            return;
        }

        HIT_COOLDOWNS.put(this.getUuid(), currentTime);

        PlayerEntity owner = null;
        if (this.getOwnerUuid() != null) {
            owner = serverWorld.getPlayerByUuid(this.getOwnerUuid());
        }

        if (owner == null) {
            return;
        }

        int amplifier = FatedVictoryEffect.getAmplifierForMergeCount(mergeCount);
        double aoeRadius = getScaledAOERadius();

        Box aoeBox = new Box(
                target.getX() - aoeRadius,
                target.getY() - aoeRadius,
                target.getZ() - aoeRadius,
                target.getX() + aoeRadius,
                target.getY() + aoeRadius,
                target.getZ() + aoeRadius
        );

        List<LivingEntity> nearbyEntities = serverWorld.getEntitiesByClass(
                LivingEntity.class,
                aoeBox,
                entity -> entity.isAlive() && entity != this && entity.getUuid() != this.getUuid()
        );

        RegistryEntry<net.minecraft.entity.effect.StatusEffect> witheringFate = TMSStatusEffects.WITHERING_FATE;
        target.addStatusEffect(new StatusEffectInstance(
                witheringFate,
                EFFECT_DURATION,
                amplifier,
                false,
                true,
                true
        ));
        spawnEnemyParticles(serverWorld, target);

        for (LivingEntity entity : nearbyEntities) {
            if (entity == target || entity.getUuid().equals(target.getUuid())) {
                continue;
            }

            if (entity.getUuid().equals(this.getUuid())) {
                continue;
            }

            EntityRelation relation = EntityRelations.getRelation(owner, entity);

            boolean isOwnerSummon = isSummonOfOwner(entity, owner);

            if (isOwnerSummon) {
                relation = EntityRelation.ALLY;
            }

            switch (relation) {
                case ALLY, FRIENDLY -> {
                    RegistryEntry<net.minecraft.entity.effect.StatusEffect> fatedVictory = TMSStatusEffects.FATED_VICTORY;
                    entity.addStatusEffect(new StatusEffectInstance(
                            fatedVictory,
                            EFFECT_DURATION,
                            amplifier,
                            false,
                            true,
                            true
                    ));

                    spawnAllyParticles(serverWorld, entity);
                }
                case HOSTILE -> {
                    entity.addStatusEffect(new StatusEffectInstance(
                            witheringFate,
                            EFFECT_DURATION,
                            amplifier,
                            false,
                            true,
                            true
                    ));

                    spawnEnemyParticles(serverWorld, entity);
                }
            }
        }

        spawnCentralParticles(serverWorld, target, aoeRadius);
    }

    private boolean isSummonOfOwner(LivingEntity entity, PlayerEntity owner) {
        if (SummonTracker.isSpellSummon(entity.getUuid())) {
            var summonData = SummonTracker.getSummonData(entity.getUuid());
            if (summonData != null && summonData.ownerUuid.equals(owner.getUuid())) {
                return true;
            }
        }

        if (entity instanceof net.minecraft.entity.Ownable ownable) {
            Entity entityOwner = ownable.getOwner();
            if (entityOwner != null && entityOwner.getUuid().equals(owner.getUuid())) {
                return true;
            }
        }

        if (entity.getScoreboardTeam() != null && owner.getScoreboardTeam() != null) {
            if (entity.getScoreboardTeam().isEqual(owner.getScoreboardTeam())) {
                return true;
            }
        }

        return false;
    }

    private void spawnAllyParticles(ServerWorld world, LivingEntity entity) {
        world.spawnParticles(
                ParticleTypes.HAPPY_VILLAGER,
                entity.getX(),
                entity.getY() + entity.getHeight() / 2,
                entity.getZ(),
                15,
                0.3, 0.5, 0.3,
                0.05
        );

        world.spawnParticles(
                ParticleTypes.END_ROD,
                entity.getX(),
                entity.getY() + 0.5,
                entity.getZ(),
                10,
                0.2, 0.3, 0.2,
                0.02
        );
    }

    private void spawnEnemyParticles(ServerWorld world, LivingEntity entity) {
        world.spawnParticles(
                ParticleTypes.CRIMSON_SPORE,
                entity.getX(),
                entity.getY() + entity.getHeight() / 2,
                entity.getZ(),
                15,
                0.3, 0.5, 0.3,
                0.05
        );

        world.spawnParticles(
                ParticleTypes.SMOKE,
                entity.getX(),
                entity.getY() + 0.5,
                entity.getZ(),
                10,
                0.2, 0.3, 0.2,
                0.02
        );
    }

    private void spawnCentralParticles(ServerWorld world, LivingEntity target, double aoeRadius) {
        world.spawnParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                target.getX(),
                target.getY() + 1.0,
                target.getZ(),
                30,
                0.5, 0.8, 0.5,
                0.1
        );

        for (int i = 0; i < 16; i++) {
            double angle = (Math.PI * 2 * i) / 16;
            double offsetX = Math.cos(angle) * aoeRadius;
            double offsetZ = Math.sin(angle) * aoeRadius;

            world.spawnParticles(
                    ParticleTypes.ENCHANT,
                    target.getX() + offsetX,
                    target.getY() + 0.5,
                    target.getZ() + offsetZ,
                    2,
                    0.1, 0.2, 0.1,
                    0.05
            );
        }
    }

    @Override
    public String getSummonType() {
        return TrueFreyrSwordDeliveryHandler.TRUE_FREYR_SWORD_TYPE;
    }

    @Override
    public int getSlotCost() {
        return TrueFreyrSwordDeliveryHandler.SLOT_COST;
    }

    @Override
    public Entity asEntity() {
        return this;
    }

    @Override
    public void remove(RemovalReason reason) {
        HIT_COOLDOWNS.remove(this.getUuid());
        super.remove(reason);
    }
}