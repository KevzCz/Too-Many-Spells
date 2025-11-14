package net.pixeldreamstudios.tms.spell.handler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.EvokerFangsEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.pixeldreamstudios.summonerlib.api.ISummonable;
import net.pixeldreamstudios.summonerlib.api.SummonBuilder;
import net.pixeldreamstudios.tms.effect.MarkedForDeathEffect;
import net.pixeldreamstudios.tms.registry.TMSStatusEffects;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellHelper;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchools;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SummonEvokerFangsHandler implements SpellHandlers.CustomDelivery {

    private static final int EFFECT_DURATION = 300;
    public static final String EVOKER_FANG_SUMMON_TYPE = "too-many-spells:evoker_fang";

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

        if (targets.isEmpty()) {
            return false;
        }

        SpellHelper.DeliveryTarget target = targets.get(0);
        if (!(target.entity() instanceof LivingEntity livingTarget)) {
            return false;
        }

        Spell spell = spellEntry.value();
        float coefficient;
        if (spell.impacts != null && !spell.impacts.isEmpty()) {
            var impact = spell.impacts.get(0);
            if (impact.action != null && impact.action.damage != null) {
                coefficient = impact.action.damage.spell_power_coefficient;
            } else {
                coefficient = 0.5F;
            }
        } else {
            coefficient = 0.5F;
        }

        SpellPower.Result soulPowerResult = SpellPower.getSpellPower(SpellSchools.SOUL, caster);
        double soulPower = soulPowerResult.baseValue();

        double baseDamage = soulPower * coefficient;

        Vec3d casterGroundPos = new Vec3d(caster.getX(), caster.getY(), caster.getZ());
        Vec3d targetGroundPos = new Vec3d(livingTarget.getX(), livingTarget.getY(), livingTarget.getZ());
        double distance = casterGroundPos.distanceTo(targetGroundPos);

        int travelSteps = (int) Math.ceil(distance * 2);
        int ticksPerStep = 1;

        spawnTravelingDarknessParticles(serverWorld, casterGroundPos, targetGroundPos, travelSteps, ticksPerStep);

        double finalBaseDamage = baseDamage;
        int totalTravelTime = travelSteps * ticksPerStep;

        scheduleDelayed(serverWorld, () -> {
            if (livingTarget.isAlive()) {
                Vec3d hitPos = livingTarget.getPos();

                spawnSummonedEvokerFang(serverWorld, caster, hitPos, 0, finalBaseDamage);

                scheduleDelayed(serverWorld, () -> {
                    if (livingTarget.isAlive()) {
                        spawnAdjacentSummonedFangs(serverWorld, caster, hitPos, finalBaseDamage);
                    }
                }, 20);

                int amplifier = MarkedForDeathEffect.calculateAmplifier(soulPower);
                RegistryEntry<net.minecraft.entity.effect.StatusEffect> markedForDeath = TMSStatusEffects.MARKED_FOR_DEATH;

                livingTarget.addStatusEffect(new StatusEffectInstance(
                        markedForDeath,
                        EFFECT_DURATION,
                        amplifier,
                        false,
                        true,
                        true
                ));
            }
        }, totalTravelTime);

        caster.playSound(SoundEvents.ENTITY_EVOKER_CAST_SPELL, 1.0F, 1.0F);

        return true;
    }

    private void spawnTravelingDarknessParticles(ServerWorld world, Vec3d start, Vec3d end, int steps, int ticksPerStep) {
        Vec3d direction = end.subtract(start);
        Vec3d stepVec = direction.multiply(1.0 / steps);

        for (int i = 0; i < steps; i++) {
            int delay = i * ticksPerStep;
            Vec3d particlePos = start.add(stepVec.multiply(i));

            scheduleDelayed(world, () -> {
                world.spawnParticles(
                        ParticleTypes.SOUL,
                        particlePos.x,
                        particlePos.y + 0.1,
                        particlePos.z,
                        5,
                        0.2, 0.05, 0.2,
                        0.02
                );

                world.spawnParticles(
                        ParticleTypes.SMOKE,
                        particlePos.x,
                        particlePos.y + 0.1,
                        particlePos.z,
                        3,
                        0.15, 0.05, 0.15,
                        0.01
                );
            }, delay);
        }
    }

    private void spawnSummonedEvokerFang(ServerWorld world, PlayerEntity caster, Vec3d pos, int warmup, double damage) {
        SummonedEvokerFangEntity fang = new SummonedEvokerFangEntity(
                world,
                pos.x,
                pos.y,
                pos.z,
                0,
                warmup,
                caster,
                (float) damage
        );

        SummonBuilder.create(caster, fang, world)
                .withType(EVOKER_FANG_SUMMON_TYPE)
                .withLifetime(-1)
                .allowInteraction(false)
                .slotCost(0)
                .group("evoker_fangs")
                .build();
    }

    private void spawnAdjacentSummonedFangs(ServerWorld world, PlayerEntity caster, Vec3d centerPos, double damage) {
        Vec3d[] offsets = {
                new Vec3d(1.5, 0, 0),
                new Vec3d(-1.5, 0, 0),
                new Vec3d(0, 0, 1.5),
                new Vec3d(0, 0, -1.5)
        };

        for (Vec3d offset : offsets) {
            Vec3d fangPos = centerPos.add(offset);
            spawnSummonedEvokerFang(world, caster, fangPos, 10, damage);
        }
    }

    private void scheduleDelayed(ServerWorld world, Runnable task, int delayTicks) {
        if (delayTicks <= 0) {
            task.run();
        } else {
            world.getServer().execute(() -> {
                scheduleDelayed(world, task, delayTicks - 1);
            });
        }
    }

    public static class SummonedEvokerFangEntity extends EvokerFangsEntity implements ISummonable {
        private final float customDamage;

        public SummonedEvokerFangEntity(World world, double x, double y, double z, float yaw, int warmup, LivingEntity owner, float damage) {
            super(world, x, y, z, yaw, warmup, owner);
            this.customDamage = damage;
        }

        public float getCustomDamage() {
            return this.customDamage;
        }

        @Override
        public String getSummonType() {
            return EVOKER_FANG_SUMMON_TYPE;
        }

        @Override
        public int getSlotCost() {
            return 0;
        }

        @Override
        public net.minecraft.entity.Entity asEntity() {
            return this;
        }
    }
}