/*
 * Copyright 2022 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.fluid.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.quiltmc.qsl.fluid.api.CustomFluidInteracting;
import org.quiltmc.qsl.fluid.impl.QuiltFluidApiConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BoatEntity.class)
public abstract class BoatEntityMixin extends Entity implements CustomFluidInteracting {

	private final double d = 0;

	public BoatEntityMixin(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	@Redirect(method = "getWaterLevelAbove", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/tag/TagKey;)Z"))
	public boolean tick(FluidState instance, TagKey<Fluid> tag) {
		return instance.isIn(FluidTags.WATER) || instance.isIn(QuiltFluidApiConstants.QUILT_FLUIDS);
	}

	@Redirect(method = "checkBoatInWater", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/tag/TagKey;)Z"))
	public boolean tick2(FluidState instance, TagKey<Fluid> tag) {
		return instance.isIn(FluidTags.WATER) || instance.isIn(QuiltFluidApiConstants.QUILT_FLUIDS);
	}

	@Inject(method = "getUnderWaterLocation", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/tag/TagKey;)Z"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
	private void getUnderWaterLocation(CallbackInfoReturnable<BoatEntity.Location> cir, Box box, double d, int i, int j, int k, int l, int m, int n, boolean bl, BlockPos.Mutable mutable, int o, int p, int q, FluidState fluidState) {
		if (fluidState.isIn(QuiltFluidApiConstants.QUILT_FLUIDS) && d < (double) ((float) mutable.getY() + fluidState.getHeight(this.world, mutable))) {
			if (!fluidState.isSource()) {
				cir.setReturnValue(BoatEntity.Location.UNDER_FLOWING_WATER);
				cir.cancel();
			}
			bl = true;
		}
	}

	@Inject(method = "fall", at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/tag/TagKey;)Z"))
	public void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition, CallbackInfo ci) {
		if (!this.world.getFluidState(this.getBlockPos().down()).isIn(QuiltFluidApiConstants.QUILT_FLUIDS) && heightDifference < 0.0) {
			this.fallDistance -= (float) heightDifference;
		}
	}
}
