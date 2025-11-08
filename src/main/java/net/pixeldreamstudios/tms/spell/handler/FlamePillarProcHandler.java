package net.pixeldreamstudios.tms.spell.handler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.soulsweaponry.entity.projectile.noclip.FlamePillar;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.internals.SpellHelper;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchools;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class FlamePillarProcHandler implements SpellHandlers.CustomDelivery {

    private static final int SPELL_PILLAR_EVENT_ID = -1;

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

        SpellPower.Result firePowerResult = SpellPower.getSpellPower(SpellSchools.FIRE, caster);
        double firePower = firePowerResult.baseValue();

        double baseProcChance = 0.20;
        double additionalChance = (firePower * 0.003);
        double procChance = Math.min(0.8, baseProcChance + additionalChance);

        double roll = world.random.nextDouble();

        if (roll >= procChance) {
            return false;
        }

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

        if (!targets.isEmpty()) {
            SpellHelper.DeliveryTarget target = targets.get(0);
            if (target.entity() instanceof LivingEntity livingTarget) {

                SpellPower.Result.Value damageValue = firePowerResult.random();
                double scaledDamage = damageValue.amount() * coefficient;
                boolean isCrit = damageValue.isCritical();

                FlamePillar pillar = new FlamePillar(
                        serverWorld,
                        caster,
                        2.5F,
                        1,
                        SPELL_PILLAR_EVENT_ID
                );

                pillar.setDamage(scaledDamage);

                Vec3d pos = livingTarget.getPos();
                pillar.setPos(pos.x, pos.y, pos.z);

                serverWorld.spawnEntity(pillar);

                if (isCrit) {
                    serverWorld.spawnParticles(
                            net.minecraft.particle.ParticleTypes.SOUL_FIRE_FLAME,
                            pos.x, pos.y + 0.5, pos.z,
                            20,
                            0.5, 0.5, 0.5,
                            0.1
                    );
                }

                double multiPillarBaseChance = 0.05;
                double multiPillarAdditionalChance = (firePower * 0.00175);
                double multiPillarChance = Math.min(0.40, multiPillarBaseChance + multiPillarAdditionalChance);

                double multiPillarRoll = world.random.nextDouble();

                if (multiPillarRoll < multiPillarChance) {
                    float finalCoefficient = coefficient;
                    scheduleMultiPillars(serverWorld, caster, livingTarget, firePowerResult, finalCoefficient);
                }

                return true;
            }
        }

        return false;
    }

    private void scheduleMultiPillars(ServerWorld world, PlayerEntity caster, LivingEntity target,
                                      SpellPower.Result firePowerResult, float coefficient) {
        AtomicInteger pillarCount = new AtomicInteger(0);

        schedulePillar(world, caster, target, firePowerResult, coefficient, pillarCount);
    }

    private void schedulePillar(ServerWorld world, PlayerEntity caster, LivingEntity target,
                                SpellPower.Result firePowerResult, float coefficient, AtomicInteger pillarCount) {
        int currentPillar = pillarCount.getAndIncrement();

        if (currentPillar >= 4) {
            return;
        }

        int delayTicks = 10 + (currentPillar * 15);

        world.getServer().execute(() -> {
            scheduleDelayed(world, () -> {
                if (target.isAlive()) {
                    Vec3d targetPos = target.getPos();

                    double offsetX = (world.random.nextDouble() - 0.5) * 6.0;
                    double offsetZ = (world.random.nextDouble() - 0.5) * 6.0;

                    Vec3d pillarPos = targetPos.add(offsetX, 0, offsetZ);

                    SpellPower.Result.Value extraDamageValue = firePowerResult.random();
                    double extraScaledDamage = extraDamageValue.amount() * coefficient * 0.5;
                    boolean extraIsCrit = extraDamageValue.isCritical();

                    FlamePillar extraPillar = new FlamePillar(
                            world,
                            caster,
                            2.5F,
                            5,
                            SPELL_PILLAR_EVENT_ID
                    );

                    extraPillar.setDamage(extraScaledDamage);
                    extraPillar.setPos(pillarPos.x, pillarPos.y, pillarPos.z);

                    world.spawnEntity(extraPillar);

                    if (extraIsCrit) {
                        world.spawnParticles(
                                net.minecraft.particle.ParticleTypes.SOUL_FIRE_FLAME,
                                pillarPos.x, pillarPos.y + 0.5, pillarPos.z,
                                20,
                                0.5, 0.5, 0.5,
                                0.1
                        );
                    }

                    schedulePillar(world, caster, target, firePowerResult, coefficient, pillarCount);
                }
            }, delayTicks);
        });
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
}