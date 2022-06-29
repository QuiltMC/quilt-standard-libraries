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

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.ComponentContainer;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.impl.container.LazifiedComponentContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@SuppressWarnings("ConstantConditions")
@Mixin(ItemStack.class)
public abstract class MixinItemStack implements ComponentProvider { // TODO: Make sure nothing else may be broken before final PR.

	@Shadow
	private @Nullable NbtCompound nbt;
	private LazifiedComponentContainer qsl$container;

	@Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At("TAIL"))
	private void initContainer(ItemConvertible itemConvertible, int i, CallbackInfo ci) {
		this.qsl$container = LazifiedComponentContainer.builder(this)
				.orElseThrow()
				.setSaveOperation(() -> this.qsl$container.writeNbt(this.getOrCreateNbt()))
				.ticking()
				.build();
	}

	@Shadow
	public abstract NbtCompound getOrCreateNbt();

	@Inject(method = "<init>(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"))
	private void readContainer(NbtCompound nbtCompound, CallbackInfo ci) {
		this.qsl$container = LazifiedComponentContainer.builder(this)
				.orElseThrow()
				.setSaveOperation(() -> this.qsl$container.writeNbt(this.getOrCreateNbt()))
				.ticking()
				.build();

		if (this.nbt != null) {
			this.qsl$container.readNbt(this.nbt);
		}
	}

	@Inject(method = "setNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;postProcessNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
	private void readContainerAgain(NbtCompound nbt, CallbackInfo ci) {
		if (this.qsl$container != null) {
			this.qsl$container.readNbt(this.nbt);
		}
	}

	@Inject(method = "copy", at = @At(value = "RETURN", ordinal = 1))
	private void deserializeContainer(CallbackInfoReturnable<ItemStack> cir) {
		ItemStack copiedStack = cir.getReturnValue();
		NbtCompound nbt = copiedStack.getNbt();
		if (nbt != null) {
			copiedStack.getContainer().readNbt(nbt);
		}
	}

	@Inject(method = "inventoryTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;inventoryTick(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;IZ)V"))
	private void tickContainer(World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {

	}

	@Override
	public @NotNull ComponentContainer getContainer() {
		return this.qsl$container;
	}
}
