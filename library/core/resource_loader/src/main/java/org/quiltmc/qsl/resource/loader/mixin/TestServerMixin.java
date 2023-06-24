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

package org.quiltmc.qsl.resource.loader.mixin;

import java.util.Optional;

import com.mojang.datafixers.util.Either;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.resource.pack.DataPackSettings;
import net.minecraft.server.WorldStem;
import net.minecraft.test.TestServer;

import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.impl.ModResourcePackUtil;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderEventContextsImpl;

@Mixin(TestServer.class)
public class TestServerMixin {
	@ModifyArg(
			method = "create",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/world/FeatureAndDataSettings;<init>(Lnet/minecraft/resource/pack/DataPackSettings;Lnet/minecraft/feature_flags/FeatureFlagBitSet;)V"
			),
			index = 0
	)
	private static DataPackSettings replaceDefaultDataPackSettings(DataPackSettings initialDataPacks) {
		return ModResourcePackUtil.DEFAULT_SETTINGS;
	}

	@ModifyVariable(method = "create", at = @At(value = "STORE"))
	private static WorldStem onSuccessfulReloadResources(WorldStem resources) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(new ResourceLoaderEventContextsImpl.ReloadEndContext(
				resources.resourceManager(), resources.registries().getCompositeManager(), Optional.empty()
		));
		return resources; // noop
	}

	@ModifyArg(
			method = "create",
			at = @At(
					value = "INVOKE",
					target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Throwable;)V",
					remap = false
			),
			index = 1
	)
	private static Throwable onFailedReloadResources(Throwable exception) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(new ResourceLoaderEventContextsImpl.ReloadEndContext(
				null, null, Optional.of(exception)
		));
		return exception; // noop
	}
}
