package net.pixeldreamstudios.tms.mixin.soulsweapons.entity.spell;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.pixeldreamstudios.summonerlib.tracker.SummonTracker;
import net.pixeldreamstudios.tms.spell.handler.TrueFreyrSwordDeliveryHandler;
import net.pixeldreamstudios.tms.util.soulsweapons.ExtendedFreyrSwordData;
import net.soulsweaponry.entity.mobs.FreyrSwordEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(FreyrSwordEntity.class)
public class FreyrSwordSpellPositioningMixin {

    @Inject(method = "mobTick", at = @At("HEAD"), cancellable = true)
    private void customSpellSummonPositioning(CallbackInfo ci) {
        FreyrSwordEntity entity = (FreyrSwordEntity) (Object) this;

        var data = SummonTracker.getSummonData(entity.getUuid());
        if (data == null) {
            return;
        }

        boolean isNormalFreyr = ExtendedFreyrSwordData.SUMMON_TYPE.equals(data.summonType);
        boolean isTrueFreyr = TrueFreyrSwordDeliveryHandler.TRUE_FREYR_SWORD_TYPE.equals(data.summonType);

        if (!isNormalFreyr && !isTrueFreyr) {
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
            int actualIndex = getActualIndex(entity, owner, data.summonType);
            int totalSummons = getTotalSummons(owner, data.summonType);
            positionInArc(entity, owner, actualIndex, totalSummons);
            entity.setAnimationAttacking(false);
        }

        ci.cancel();
    }

    private int getActualIndex(FreyrSwordEntity entity, LivingEntity owner, String summonType) {
        if (!(owner instanceof PlayerEntity player)) {
            return 0;
        }

        List<UUID> allSummons = SummonTracker.getPlayerSummonsByType(
                player.getUuid(),
                summonType
        );
        int index = allSummons.indexOf(entity.getUuid());
        return index >= 0 ? index : 0;
    }

    private void positionInArc(FreyrSwordEntity entity, LivingEntity owner, int index, int total) {
        double radius = 2.5;
        double arcSpread = Math.PI * 0.5;

        float ownerYawRad = owner.getYaw() * (float) Math.PI / 180.0F;
        double behindAngle = ownerYawRad + Math.PI;

        double offsetAngle;
        if (total == 1) {
            offsetAngle = 0;
        } else {
            double step = arcSpread / (total - 1);
            offsetAngle = (index * step) - (arcSpread / 2.0);
        }

        double finalAngle = behindAngle + offsetAngle;
        double offsetX = -Math.sin(finalAngle) * radius;
        double offsetZ = Math.cos(finalAngle) * radius;

        Vec3d targetPos = owner.getPos().add(offsetX, 0, offsetZ);
        float swordYaw = (float) Math.toDegrees(finalAngle);

        entity.updatePositionAndAngles(
                targetPos.getX(),
                owner.getY(),
                targetPos.getZ(),
                swordYaw,
                0.0F
        );
    }

    private int getTotalSummons(LivingEntity owner, String summonType) {
        if (owner instanceof PlayerEntity player) {
            return SummonTracker.getPlayerSummonCountByType(
                    player.getUuid(),
                    summonType
            );
        }
        return 1;
    }
}