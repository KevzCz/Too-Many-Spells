package net.pixeldreamstudios.tms.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.pixeldreamstudios.tms.util.SummonTracker;
import net.soulsweaponry.entity.mobs.FreyrSwordEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FreyrSwordEntity.class)
public class FreyrSwordSpellPositioningMixin {

    @Inject(method = "mobTick", at = @At("HEAD"), cancellable = true)
    private void customSpellSummonPositioning(CallbackInfo ci) {
        FreyrSwordEntity entity = (FreyrSwordEntity) (Object) this;

        SummonTracker.SummonData data = SummonTracker.getSummonData(entity.getUuid());
        if (data == null || !data.summonType.equals("freyr_sword")) {
            return;
        }

        LivingEntity owner = entity.getOwner();
        if (owner == null) {
            return;
        }

        if (!entity.isBlockPosNullish(entity.getStationaryPos())) {
            if (entity.getTarget() == null || entity.squaredDistanceTo(entity.stationaryAsVec3d()) > entity.getFollowRange()) {
                entity.updatePosition(
                        (double) entity.getStationaryPos().getX(),
                        (double) entity.getStationaryPos().getY(),
                        (double) entity.getStationaryPos().getZ()
                );
                entity.setAnimationAttacking(false);
            }
        } else if (entity.getTarget() == null || entity.squaredDistanceTo(owner) > entity.getFollowRange()) {
            positionInArc(entity, owner, data.summonIndex, getTotalSummons(owner));
            entity.setAnimationAttacking(false);
        }

        ci.cancel();
    }

    private void positionInArc(FreyrSwordEntity entity, LivingEntity owner, int index, int total) {
        double radius = 2.5;
        double arcAngle = Math.PI * 0.6;
        double startAngle = owner.getYaw() * (Math.PI / 180.0) + Math.PI - (arcAngle / 2);

        double angleStep = total > 1 ? arcAngle / (total - 1) : 0;
        double angle = startAngle + (angleStep * index);

        double offsetX = Math.cos(angle) * radius;
        double offsetZ = Math.sin(angle) * radius;

        Vec3d targetPos = owner.getPos().add(offsetX, 0, offsetZ);

        entity.updatePositionAndAngles(
                targetPos.getX(),
                owner.getY(),
                targetPos.getZ(),
                (float) (angle * (180.0 / Math.PI)),
                0.0F
        );
    }

    private int getTotalSummons(LivingEntity owner) {
        if (owner instanceof PlayerEntity player) {
            return SummonTracker.getPlayerSummonCountByType(player.getUuid(), "freyr_sword");
        }
        return 1;
    }
}