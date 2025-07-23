/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.item.content.registry.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.HolderLookup;

@Mixin(AbstractFurnaceBlockEntity.class)
abstract class AbstractFurnaceBlockEntityMixin {
	@Shadow
	int burnTime;

	// Serializes burn time as an integer instead of a short.
	// Should not cause any desyncs as BE sync packets are now NBT.

	@Inject(method = "readNbtImpl", at = @At("TAIL"))
	private void readBurnTimeAsInt(NbtCompound nbt, HolderLookup.Provider lookupProvider, CallbackInfo info) {
		this.burnTime = nbt.getInt("lit_time_remaining").orElseThrow();
	}

	@Inject(method = "writeNbt", at = @At("TAIL"))
	private void writeBurnTimeAsInt(NbtCompound nbt, HolderLookup.Provider lookupProvider, CallbackInfo info) {
		nbt.putInt("lit_time_remaining", this.burnTime);
	}
}
