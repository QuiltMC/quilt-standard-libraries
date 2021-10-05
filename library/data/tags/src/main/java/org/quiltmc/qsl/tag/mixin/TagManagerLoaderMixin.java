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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.tag.RequiredTagList;
import net.minecraft.tag.TagManagerLoader;
import net.minecraft.util.profiler.Profiler;

import org.quiltmc.qsl.tag.impl.TagRegistryImpl;

@Mixin(TagManagerLoader.class)
public abstract class TagManagerLoaderMixin {
	@Inject(method = "reload", at = @At("HEAD"))
	private void onReload(ResourceReloader.Synchronizer synchronizer, ResourceManager manager,
	                      Profiler prepareProfiler, Profiler applyProfiler,
	                      Executor prepareExecutor, Executor applyExecutor,
	                      CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		TagRegistryImpl.forceInit();
	}

	@Dynamic("private static synthetic method_33179; " +
			"Consumer lambda in reload method, corresponds to the RequiredTagList.forEach.")
	@Inject(method = "method_33179", at = @At("HEAD"), cancellable = true)
	private void onRequiredGroupBuild(ResourceManager resourceManager, Executor executor, List<?> list,
	                                  RequiredTagList<?> requiredTagList, CallbackInfo ci) {
		// Don't load dynamic registry tags now, we need to load them after the dynamic registry.
		if (TagRegistryImpl.isRegistryDynamic(requiredTagList.getRegistryKey())) {
			ci.cancel();
		}
	}
}
