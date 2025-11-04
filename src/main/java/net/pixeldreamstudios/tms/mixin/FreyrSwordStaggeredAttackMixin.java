package net.pixeldreamstudios.tms.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import net.pixeldreamstudios.tms.util.SummonTracker;
import net.soulsweaponry.entity.ai.goal.FreyrSwordGoal;
import net.soulsweaponry.entity.mobs.FreyrSwordEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FreyrSwordGoal.class)
public class FreyrSwordStaggeredAttackMixin {

    @Shadow @Final private FreyrSwordEntity entity;
    @Shadow private int attackTicks;

    private static final int ATTACK_DELAY_PER_SWORD = 5;

    @Inject(method = "attackTarget", at = @At("HEAD"))
    private void applyStaggeredDelay(LivingEntity target, World world, CallbackInfo ci) {
        SummonTracker.SummonData data = SummonTracker.getSummonData(entity.getUuid());
        if (data != null && data.summonIndex > 0) {
            int delay = data.summonIndex * ATTACK_DELAY_PER_SWORD;

            if (attackTicks < delay) {
                attackTicks = -1;
            }
        }
    }
}