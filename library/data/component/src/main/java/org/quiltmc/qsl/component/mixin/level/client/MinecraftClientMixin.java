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

package org.quiltmc.qsl.component.mixin.level.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;

import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin implements ComponentProvider {
	private ComponentContainer qsl$container;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void initContainer(RunArgs runArgs, CallbackInfo ci) {
		this.qsl$container = ComponentContainer.builder(this).build(ComponentContainer.LAZY_FACTORY);
	}

	@Override
	public ComponentContainer getComponentContainer() {
		return this.qsl$container;
	}
}
