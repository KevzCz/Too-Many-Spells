package net.pixeldreamstudios.tms.mixin.soulsweapons.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.pixeldreamstudios.tms.config.TMSSoulsweaponsConfig;
import net.pixeldreamstudios.tms.util.PetInheritanceUtil;
import net.soulsweaponry.entity.mobs.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Remnant.class)
public abstract class RemnantSpawnMixin {

    @Unique
    private boolean tmscompat$attributesApplied = false;

    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void tmscompat$applyOwnerStatsOnce(CallbackInfo ci) {
        Remnant remnant = (Remnant) (Object) this;

        if (tmscompat$attributesApplied || remnant.getWorld().isClient) {
            return;
        }

        if (remnant.isTamed() && remnant.getOwner() instanceof PlayerEntity owner) {
            Double ratio = null;
            boolean useKevsLibrary = true;

            if (remnant instanceof Forlorn || remnant instanceof Soulmass || remnant instanceof SoulReaperGhost) {
                TMSSoulsweaponsConfig.SoulReaperConfig config = TMSSoulsweaponsConfig.getInstance().soul_reaper;
                useKevsLibrary = config.useKevslibraryPetInheritanceAttribute;
                if (!useKevsLibrary) {
                    ratio = config.petInheritanceBonus;
                }
            } else if (remnant instanceof FrostGiant || remnant instanceof RimeSpectre) {
                TMSSoulsweaponsConfig.FrostmourneConfig config = TMSSoulsweaponsConfig.getInstance().frostmourne;
                useKevsLibrary = config.useKevslibraryPetInheritanceAttribute;
                if (!useKevsLibrary) {
                    ratio = config.petInheritanceBonus;
                }
            } else if (remnant.getClass() == Remnant.class) {
                TMSSoulsweaponsConfig.NightfallConfig config = TMSSoulsweaponsConfig.getInstance().nightfall;
                useKevsLibrary = config.useKevslibraryPetInheritanceAttribute;
                if (!useKevsLibrary) {
                    ratio = config.petInheritanceBonus;
                }
            }

            if (ratio != null && !useKevsLibrary) {
                NbtCompound previousData = new NbtCompound();
                PetInheritanceUtil.apply(owner, remnant, previousData, ratio);
                tmscompat$attributesApplied = true;
            }
        }
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    private void tmscompat$writeAttributesApplied(NbtCompound nbt, CallbackInfo ci) {
        nbt.putBoolean("tmscompat_attributes_applied", tmscompat$attributesApplied);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    private void tmscompat$readAttributesApplied(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("tmscompat_attributes_applied")) {
            tmscompat$attributesApplied = nbt.getBoolean("tmscompat_attributes_applied");
        }
    }
}