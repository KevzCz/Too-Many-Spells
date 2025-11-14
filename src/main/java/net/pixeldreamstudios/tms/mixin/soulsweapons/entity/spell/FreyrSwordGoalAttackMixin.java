package net.pixeldreamstudios.tms.mixin.soulsweapons.entity.spell;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.pixeldreamstudios.summonerlib.api.ISummonable;
import net.pixeldreamstudios.summonerlib.util.SummonCritUtil;
import net.pixeldreamstudios.tms.entity.soulsweapons.TrueFreyrSwordEntity;
import net.soulsweaponry.entity.ai.goal.FreyrSwordGoal;
import net.soulsweaponry.entity.mobs.FreyrSwordEntity;
import net.soulsweaponry.particles.ParticleHandler;
import net.soulsweaponry.registry.DamageSourceRegistry;
import net.soulsweaponry.util.WeaponUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(FreyrSwordGoal.class)
public class FreyrSwordGoalAttackMixin {

    @Shadow @Final private FreyrSwordEntity entity;
    @Shadow @Final private double[][] hitFrames;
    @Shadow private int attackTicks;

    @Shadow
    public float getAttackDamage(LivingEntity target) {
        return 0;
    }

    @Inject(
            method = "attackTarget",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void attackTargetWithAOE(LivingEntity target, World world, CallbackInfo ci) {

        if (!(entity instanceof TrueFreyrSwordEntity)) {
            return;
        }

        ci.cancel();

        ++attackTicks;
        if ((double)attackTicks >= 3.5 * 10.0) {
            attackTicks = 0;
        }

        entity.getLookControl().lookAt(target);

        Vec3d vecTarget = entity.getRotationVector().add(target.getPos());
        entity.updatePosition(vecTarget.getX(), vecTarget.getY(), vecTarget.getZ());

        entity.setAnimationAttacking(true);

        double scale = entity.getAttributeValue(EntityAttributes.GENERIC_SCALE);
        double aoeRadius = 2.0 + (scale * 1.5);

        for (double[] hitFrame : hitFrames) {
            if ((double)attackTicks == hitFrame[0]) {

                Box aoeBox = new Box(
                        target.getX() - aoeRadius,
                        target.getY() - aoeRadius / 2,
                        target.getZ() - aoeRadius,
                        target.getX() + aoeRadius,
                        target.getY() + aoeRadius / 2,
                        target.getZ() + aoeRadius
                );

                List<LivingEntity> nearbyEntities = world.getEntitiesByClass(
                        LivingEntity.class,
                        aoeBox,
                        e -> e.isAlive() && e != entity && canAttack(e)
                );


                if (!nearbyEntities.contains(target) && target.isAlive()) {
                    nearbyEntities.add(target);
                }


                for (LivingEntity aoeTarget : nearbyEntities) {
                    float damage = getAttackDamage(aoeTarget) * (float)hitFrame[1];


                    double distance = aoeTarget.distanceTo(target);
                    float distanceMultiplier = (float) Math.max(0.5, 1.0 - (distance / aoeRadius) * 0.5);
                    damage *= distanceMultiplier;

                    boolean hit = aoeTarget.damage(
                            DamageSourceRegistry.create(
                                    entity.getWorld(),
                                    DamageSourceRegistry.FREYR_SWORD,
                                    entity,
                                    entity.getOwner()
                            ),
                            damage
                    );

                    if (hit) {

                        int fire = WeaponUtil.getLevel(entity.getStack(), Enchantments.FIRE_ASPECT);
                        if (fire > 0) {
                            aoeTarget.setOnFireFor((float)(fire * 4));
                        }


                        if (!world.isClient) {
                            ParticleHandler.singleParticle(
                                    entity.getWorld(),
                                    ParticleTypes.SWEEP_ATTACK,
                                    aoeTarget.getX(),
                                    aoeTarget.getEyeY(),
                                    aoeTarget.getZ(),
                                    0.0, 0.0, 0.0
                            );
                        } else {
                            world.addParticle(
                                    ParticleTypes.SWEEP_ATTACK,
                                    true,
                                    aoeTarget.getX(),
                                    aoeTarget.getEyeY(),
                                    aoeTarget.getZ(),
                                    0.0, 0.0, 0.0
                            );
                        }


                        if (entity instanceof ISummonable summonable) {
                            boolean wasCrit = SummonCritUtil.wasLastDamageCrit();
                            summonable.onSummonHit(aoeTarget, damage, wasCrit);
                        }
                    }
                }


                spawnAOEParticles(world, target, aoeRadius);

                world.playSound(
                        (PlayerEntity)null,
                        entity.getBlockPos(),
                        SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                        SoundCategory.NEUTRAL,
                        0.8F,
                        1.0F
                );
            }
        }
    }

    private boolean canAttack(LivingEntity target) {
        if (entity.getOwner() == null) {
            return false;
        }


        if (target == entity.getOwner()) {
            return false;
        }


        if (target instanceof net.minecraft.entity.Tameable tameable) {
            if (tameable.getOwner() == entity.getOwner()) {
                return false;
            }
        }


        return entity.canTarget(target);
    }

    private void spawnAOEParticles(World world, LivingEntity center, double radius) {
        if (world instanceof ServerWorld serverWorld) {

            for (int i = 0; i < 24; i++) {
                double angle = (Math.PI * 2 * i) / 24;
                double offsetX = Math.cos(angle) * radius;
                double offsetZ = Math.sin(angle) * radius;

                serverWorld.spawnParticles(
                        ParticleTypes.SOUL_FIRE_FLAME,
                        center.getX() + offsetX,
                        center.getY() + 0.1,
                        center.getZ() + offsetZ,
                        1,
                        0.1, 0.1, 0.1,
                        0.01
                );
            }


            serverWorld.spawnParticles(
                    ParticleTypes.EXPLOSION,
                    center.getX(),
                    center.getY() + 0.5,
                    center.getZ(),
                    3,
                    radius * 0.3, 0.2, radius * 0.3,
                    0.0
            );
        }
    }
}