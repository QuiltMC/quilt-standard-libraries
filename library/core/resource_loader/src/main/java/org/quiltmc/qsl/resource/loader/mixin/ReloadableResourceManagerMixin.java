/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2021 The Quilt Project
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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resource.AutoCloseableResourceManager;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceReload;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.util.Unit;

import org.quiltmc.qsl.resource.loader.api.GroupResourcePack;
import org.quiltmc.qsl.resource.loader.impl.QuiltMultiPackResourceManagerHooks;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

@Mixin(ReloadableResourceManager.class)
public class ReloadableResourceManagerMixin {
	@Final
	@Shadow
	private ResourceType type;

	@Final
	@Shadow
	private List<ResourceReloader> reloaders;

	@Shadow
	private AutoCloseableResourceManager resources;

	@Inject(method = "reload", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;isDebugEnabled()Z", remap = false))
	private void reload(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage,
			List<ResourcePack> packs, CallbackInfoReturnable<ResourceReload> info) {
		if (this.resources instanceof QuiltMultiPackResourceManagerHooks hooks) {
			hooks.quilt$appendTopPacks();
		}

		ResourceLoaderImpl.sort(this.type, this.reloaders);
	}

	/**
	 * private static synthetic method_29491(Ljava/util/List;)Ljava/lang/Object;
	 * Supplier lambda in beginMonitoredReload method.
	 * <p>
	 * This is an injector since Mixin doesn't like the Overwrite for some reason,
	 * despite {@code remap = false} and {@link Dynamic}.
	 *
	 * @author The Quilt Project, LambdAurora
	 * @reason To allow the printing of the full name of group resource packs.
	 */
	@Dynamic
	@Inject(method = "method_29491(Ljava/util/List;)Ljava/lang/Object;", at = @At("HEAD"), cancellable = true)
	private static void getResourcePackNames(List<ResourcePack> packs, CallbackInfoReturnable<String> cir) {
		cir.setReturnValue(packs.stream().map(pack -> {
			if (pack instanceof GroupResourcePack groupResourcePack) {
				return groupResourcePack.getFullName();
			} else {
				return pack.getName();
			}
		}).collect(Collectors.joining(", ")));
	}
}
