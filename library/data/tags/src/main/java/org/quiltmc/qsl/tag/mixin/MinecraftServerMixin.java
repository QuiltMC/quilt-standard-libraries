/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.tag.mixin;

import java.util.Collection;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.DynamicRegistryManager;

import org.quiltmc.qsl.tag.impl.TagRegistryImpl;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Shadow
	@Final
	protected DynamicRegistryManager.Impl registryManager;

	@Dynamic("Lambda method in MinecraftServer#reloadResources.")
	@Inject(
			method = "method_29440",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resource/ServerResourceManager;loadRegistryTags()V",
					shift = At.Shift.AFTER
			),
			remap = false
	)
	private void method_29440(Collection<?> collection, ServerResourceManager serverResourceManager, CallbackInfo ci) {
		// Load dynamic registry tags on data pack reload.
		TagRegistryImpl.loadDynamicRegistryTags(registryManager, serverResourceManager.getResourceManager());
	}
}
