package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.soulsweaponry.items.spear.DragonslayerSwordspear;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(DragonslayerSwordspear.class)
public abstract class DragonslayerSwordspearStormAttributionMixin {
    @Unique
    private static final ThreadLocal<LivingEntity> tmscompat$user = ThreadLocal.withInitial(() -> null);

    @Inject(method = "onStoppedUsing", at = @At("HEAD"))
    private void tmscompat$storeUser(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        tmscompat$user.set(user);
    }

    @Inject(method = "onStoppedUsing", at = @At("TAIL"))
    private void tmscompat$clearUser(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        tmscompat$user.remove();
    }

    @ModifyArg(
            method = "onStoppedUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"),
            index = 0
    )
    private Entity tmscompat$attributeLightning(Entity e) {
        LivingEntity u = tmscompat$user.get();
        if (e instanceof LightningEntity && u instanceof ServerPlayerEntity sp) {
            ((LightningEntity) e).setChanneler(sp);
        }
        return e;
    }
}
