package org.quiltmc.qsl.entity_events.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import org.quiltmc.qsl.entity_events.api.LivingEntityDeathCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = { LivingEntity.class, ServerPlayerEntity.class })
public abstract class LivingEntityDeathEventMixin extends Entity {
	public LivingEntityDeathEventMixin(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageTracker;update()V"))
	void quilt$invokeLivingEntityDeathEvent(DamageSource source, CallbackInfo ci) {
		if (!this.world.isClient()) {
			LivingEntityDeathCallback.EVENT.invoker().onDeath((LivingEntity) (Object) this, source);
		}
	}
}
