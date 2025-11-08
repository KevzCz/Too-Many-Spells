package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.soulsweaponry.entity.projectile.noclip.DamagingWarmupEntity;
import net.soulsweaponry.entity.projectile.noclip.DamagingWarmupEntityEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = DamagingWarmupEntity.class)
public abstract class MjolnirWarmupLightningAttributionMixin {

    @Inject(method = "onTrigger", at = @At("HEAD"), cancellable = true, remap = false)
    private void tmscompat$attributeWarmupLightningToPlayer(CallbackInfo ci) {
        DamagingWarmupEntity self = (DamagingWarmupEntity) (Object) this;
        if (self.getEventId() != DamagingWarmupEntityEvents.SPAWN_LIGHTNING) {
            return;
        }

        World world = self.getWorld();
        if (world.isSkyVisible(self.getBlockPos())) {
            LightningEntity bolt = new LightningEntity(EntityType.LIGHTNING_BOLT, world);
            bolt.setPos(self.getX(), self.getY(), self.getZ());
            if (self.getOwner() instanceof ServerPlayerEntity sp) {
                bolt.setChanneler(sp);
            }
            world.spawnEntity(bolt);
        }

        ci.cancel();
    }
}
