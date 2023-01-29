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

package org.quiltmc.qsl.component.mixin.block.entity;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.entity.BlockEntity;

@Mixin(targets = "net/minecraft/world/chunk/WorldChunk$DirectBlockEntityTickInvoker")
public class DirectBlockEntityTickInvokerMixin<T extends BlockEntity> {
	@Shadow
	@Final
	private T blockEntity;

	@SuppressWarnings("ConstantConditions")
	@Inject(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/entity/BlockEntityTicker;tick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BlockEntity;)V",
					shift = At.Shift.AFTER
			)
	)
	private void tickContainer(CallbackInfo ci) {
		if (!this.blockEntity.getWorld().isClient) { // we know the block entity will have a world if it is contained in a ticker
			this.blockEntity.getComponentContainer().tick(this.blockEntity);
		}
	}
}
