/*
 * Copyright 2022-2023 QuiltMC
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

import java.util.Collection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resource.pack.DataPackSettings;
import net.minecraft.resource.pack.ResourcePackManager;
import net.minecraft.server.WorldStem;
import net.minecraft.test.GameTestBatch;
import net.minecraft.test.TestServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSaveStorage;

import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.impl.ModResourcePackUtil;

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

	@Inject(
			method = "create",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/Util;method_43499(Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;"
			)
	)
	private static void onStartReloadResources(Thread thread, WorldSaveStorage.Session session, ResourcePackManager packManager,
			Collection<GameTestBatch> testBatch, BlockPos pos, CallbackInfoReturnable<TestServer> cir) {
		ResourceLoaderEvents.START_DATA_PACK_RELOAD.invoker().onStartDataPackReload(null, null); // First reload
	}

	@ModifyVariable(method = "create", at = @At(value = "STORE"))
	private static WorldStem onSuccessfulReloadResources(WorldStem resources) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, resources.resourceManager(), null);
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
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, null, exception);
		return exception; // noop
	}
}
