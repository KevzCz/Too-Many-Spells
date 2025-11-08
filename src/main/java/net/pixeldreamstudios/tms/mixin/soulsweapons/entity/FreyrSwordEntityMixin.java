package net.pixeldreamstudios.tms.mixin.soulsweapons.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.pixeldreamstudios.tms.config.TMSSoulsweaponsConfig;
import net.pixeldreamstudios.tms.util.PetInheritanceUtil;
import net.pixeldreamstudios.tms.util.soulsweapons.ExtendedFreyrSwordData;
import net.soulsweaponry.entity.mobs.FreyrSwordEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FreyrSwordEntity.class)
public abstract class FreyrSwordEntityMixin {

    @Unique
    private boolean tmscompat$attributesApplied = false;

    @Inject(method = "mobTick", at = @At("HEAD"))
    private void tmscompat$applyOwnerStatsOnce(CallbackInfo ci) {
        FreyrSwordEntity freyrSword = (FreyrSwordEntity) (Object) this;

        if (tmscompat$attributesApplied || freyrSword.getWorld().isClient) {
            return;
        }

        if (ExtendedFreyrSwordData.isSpellSummon(freyrSword.getUuid())) {
            tmscompat$attributesApplied = true;
            return;
        }

        if (freyrSword.isTamed() && freyrSword.getOwner() instanceof PlayerEntity owner) {
            TMSSoulsweaponsConfig.FreyrSwordConfig config = TMSSoulsweaponsConfig.getInstance().freyr_sword;

            if (!config.useKevslibraryPetInheritanceAttribute) {
                NbtCompound previousData = new NbtCompound();
                double ratio = config.petInheritanceBonus;
                PetInheritanceUtil.apply(owner, freyrSword, previousData, ratio);
                tmscompat$attributesApplied = true;
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void tmsompat$writeAttributesApplied(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("tmscompat_attributes_applied", tmscompat$attributesApplied);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void tmscompat$readAttributesApplied(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("tmscompat_attributes_applied")) {
            tmscompat$attributesApplied = nbt.getBoolean("tmscompat_attributes_applied");
        }
    }
}