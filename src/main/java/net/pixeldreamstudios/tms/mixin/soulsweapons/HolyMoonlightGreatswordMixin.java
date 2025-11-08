package net.pixeldreamstudios.tms.mixin.soulsweapons;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.pixeldreamstudios.tms.config.ConfigHelper;
import net.soulsweaponry.config.ConfigConstructor;
import net.soulsweaponry.items.sword.HolyMoonlightGreatsword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = HolyMoonlightGreatsword.class)
public abstract class HolyMoonlightGreatswordMixin {
	@Unique private static final ThreadLocal<Float> tmscompat$scale = ThreadLocal.withInitial(() -> 1.0F);

	@Inject(
			method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
			at = @At("HEAD"),
			require = 0
	)
	private void tmscompat$cacheScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
		float factor = 1.0F;
		if (user != null) {
			float adBaseline = ConfigHelper.getBaselineValue("holy_moonlight_greatsword.attack_damage_baseline", 10.0F);

			if (adBaseline > 0.0F) {
				double ad = user.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
				if (ad > 0.0) {
					factor = (float) (ad / adBaseline);
				}
			}
		}
		tmscompat$scale.set(factor);
	}

	@Inject(
			method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
			at = @At("TAIL"),
			require = 0
	)
	private void tmscompat$clearScale(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
		tmscompat$scale.remove();
	}

	@Redirect(
			method = "onStoppedUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;I)V",
			at = @At(
					value = "FIELD",
					target = "Lnet/soulsweaponry/config/ConfigConstructor;holy_moonlight_ability_damage:F",
					remap = false
			),
			require = 0
	)
	private float tmscompat$scaleAbilityField() {
		return ConfigConstructor.holy_moonlight_ability_damage * tmscompat$scale.get();
	}

	@Inject(
			method = "getAbilityDamage()F",
			at = @At("RETURN"),
			cancellable = true,
			require = 0,
			remap = false
	)
	private void tmscompat$scaleAbilityDamage(CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(cir.getReturnValueF() * tmscompat$scale.get());
	}
}