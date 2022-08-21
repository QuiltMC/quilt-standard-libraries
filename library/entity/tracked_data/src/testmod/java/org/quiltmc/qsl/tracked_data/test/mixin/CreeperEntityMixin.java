package org.quiltmc.qsl.tracked_data.test.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.quiltmc.qsl.tracked_data.test.TrackedDataTestInitializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Don't do that in actual mod!
 */

@Mixin(CreeperEntity.class)
public class CreeperEntityMixin extends HostileEntity {
	private static final TrackedData<ParticleEffect> PARTICLE = DataTracker.registerData(CreeperEntity.class, TrackedDataTestInitializer.PARTICLE_DATA_HANDLER);


	protected CreeperEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "initDataTracker", at = @At("TAIL"))
	private void quiltTestMod$addCustomTracker(CallbackInfo ci) {
		this.dataTracker.startTracking(PARTICLE, ParticleTypes.ANGRY_VILLAGER);
	}

	@Inject(method = "interactMob", at = @At("HEAD"))
	private void quiltTestMod$addCustomTracker(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		if (this.world instanceof ServerWorld serverWorld) {
			if (player.getStackInHand(hand).getItem() == Items.STICK) {
				this.dataTracker.set(PARTICLE, ParticleTypes.CRIT);
			} else if (player.getStackInHand(hand).getItem() == Items.TNT) {
				this.dataTracker.set(PARTICLE, ParticleTypes.SMOKE);
			} else {
				serverWorld.spawnParticles(this.dataTracker.get(PARTICLE), this.getX(), this.getY() + 2, this.getZ(), 5, 1, 1, 1, 0);
			}
		}

	}
}
