/*
 * Copyright 2025 The Quilt Project
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

package org.quiltmc.qsl.entity.test.mixin.networking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.render.entity.state.CreeperRenderState;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.entity.test.networking.CreeperStateWithItem;

@Mixin(CreeperRenderState.class)
abstract class CreeperRenderStateMixin implements CreeperStateWithItem {
	@Unique
	private ItemStack stackToDrop;

	@Unique
	private float stackRotation;

	@Override
	public ItemStack quilt$getStack() {
		return this.stackToDrop;
	}

	@Override
	public void quilt$setStack(ItemStack stack) {
		this.stackToDrop = stack;
	}

	@Override
	public float quilt$getStackRotation() {
		return this.stackRotation;
	}

	@Override
	public void quilt$setStackRotation(float rotation) {
		this.stackRotation = rotation;
	}
}
