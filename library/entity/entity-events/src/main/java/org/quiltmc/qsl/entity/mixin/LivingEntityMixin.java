package org.quiltmc.qsl.entity.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.quiltmc.qsl.entity.api.event.EntityKilledCallback;
import org.quiltmc.qsl.entity.api.event.TryReviveCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	@Inject(method = "onKilledBy", at = @At(value = "HEAD"))
	private void onEntityKilledOther(LivingEntity adversary, CallbackInfo ci) {
		EntityKilledCallback.EVENT.invoker().onKilled(((LivingEntity) (Object) this).world, adversary, (LivingEntity) (Object) this);
	}

	@Inject(method = "tryUseTotem", at = @At("HEAD"), cancellable = true)
	void invokeTryReviveBeforeTotemEvent(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if (TryReviveCallback.BEFORE_TOTEM.invoker().tryRevive((LivingEntity) (Object) this, source)) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "tryUseTotem", at = @At("RETURN"), cancellable = true)
	void invokeTryReviveAfterTotemEvent(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if (!cir.getReturnValue()) {
			cir.setReturnValue(TryReviveCallback.AFTER_TOTEM.invoker().tryRevive((LivingEntity) (Object) this, source));
		}
	}
}
