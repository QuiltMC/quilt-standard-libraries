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

package org.quiltmc.qsl.component.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.component.api.ComponentContainer;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.impl.container.LazyComponentContainer;
import org.quiltmc.qsl.component.impl.sync.SyncPlayerList;
import org.quiltmc.qsl.component.impl.sync.header.SyncPacketHeader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntity.class)
public abstract class MixinBlockEntity implements ComponentProvider {
	private ComponentContainer qsl$container;

	@Shadow
	public abstract void markDirty();

	@Override
	public @NotNull ComponentContainer getComponentContainer() {
		return this.qsl$container;
	}

	@Inject(method = "readNbt", at = @At("TAIL"))
	private void onReadNbt(NbtCompound nbt, CallbackInfo ci) {
		this.qsl$container.readNbt(nbt);
	}

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onInit(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
		this.qsl$container = LazyComponentContainer.builder(this)
				.orElseThrow()
				.saving(this::markDirty)
				.ticking()
				.syncing(SyncPacketHeader.BLOCK_ENTITY, () -> SyncPlayerList.create((BlockEntity) (Object) this))
				.build();
	}

	@Inject(method = "toNbt", at = @At("TAIL"))
	private void onWriteNbt(CallbackInfoReturnable<NbtCompound> cir) {
		this.qsl$container.writeNbt(cir.getReturnValue());
	}
}
