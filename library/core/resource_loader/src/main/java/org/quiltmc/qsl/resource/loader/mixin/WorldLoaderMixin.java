/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.resource.loader.mixin;

import java.lang.ref.WeakReference;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.common.base.Suppliers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resource.MultiPackResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.WorldLoader;

import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderEventContextsImpl;

@Mixin(WorldLoader.class)
public class WorldLoaderMixin {
	@Inject(
			method = "load",
			at = @At("HEAD")
	)
	private static <D, R> void onLoad(
			WorldLoader.InitConfig initConfig,
			WorldLoader.LoadContextSupplier<D> loadContextSupplier, WorldLoader.ApplierFactory<D, R> applierFactory,
			Executor prepareExecutor, Executor applyExecutor,
			CallbackInfoReturnable<CompletableFuture<R>> cir
	) {
		ResourceLoaderEventContextsImpl.server = null;
		ResourceLoaderEvents.START_DATA_PACK_RELOAD.invoker().onStartDataPackReload(new ResourceLoaderEventContextsImpl.ReloadStartContext(
				Suppliers.memoize(() -> new MultiPackResourceManager(ResourceType.SERVER_DATA, initConfig.packConfig().packManager().createResourcePacks())),
				null
		));
	}
}
