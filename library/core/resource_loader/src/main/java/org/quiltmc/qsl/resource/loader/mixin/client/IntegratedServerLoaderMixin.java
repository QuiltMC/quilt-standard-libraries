/*
 * Copyright 2021-2023 QuiltMC
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

package org.quiltmc.qsl.resource.loader.mixin.client;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.feature_flags.FeatureFlags;
import net.minecraft.registry.LayeredRegistryManager;
import net.minecraft.resource.AutoCloseableResourceManager;
import net.minecraft.resource.pack.ResourcePackManager;
import net.minecraft.server.ServerReloadableResources;
import net.minecraft.server.WorldLoader;
import net.minecraft.server.WorldStem;
import net.minecraft.server.integrated.IntegratedServerLoader;
import net.minecraft.world.storage.WorldSaveStorage;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.util.TriState;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

@ClientOnly
@Mixin(IntegratedServerLoader.class)
public abstract class IntegratedServerLoaderMixin {
	@Shadow
	private static void close(WorldSaveStorage.Session storageSession, String worlName) {
		throw new IllegalStateException("Mixin injection failed.");
	}

	@Shadow
	protected abstract void start(Screen parentScreen, String worldName, boolean safeMode, boolean requireBackup);

	@Unique
	private static final TriState EXPERIMENTAL_SCREEN_OVERRIDE = TriState.fromProperty("quilt.resource_loader.experimental_screen_override");

	@Inject(
			method = "m_ocpkzrtb",
			at = @At("HEAD")
	)
	private <D, R> void onStartDataPackLoad(WorldLoader.PackConfig packConfig, WorldLoader.LoadContextSupplier<D> loadContextSupplier,
			WorldLoader.ApplierFactory<D, R> applierFactory, CallbackInfoReturnable<R> cir) {
		ResourceLoaderEvents.START_DATA_PACK_RELOAD.invoker().onStartDataPackReload(null, null);
	}

	@Inject(
			method = "m_ocpkzrtb",
			at = @At("RETURN")
	)
	private <D, R> void onEndDataPackLoad(WorldLoader.PackConfig packConfig, WorldLoader.LoadContextSupplier<D> loadContextSupplier,
			WorldLoader.ApplierFactory<D, R> applierFactory, CallbackInfoReturnable<R> cir) {
		if (cir.getReturnValue() instanceof WorldStem worldStem) {
			ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, worldStem.resourceManager(), null);
		}
	}

	@Dynamic
	@Inject(
			method = "m_weunchkk(Lnet/minecraft/resource/AutoCloseableResourceManager;Lnet/minecraft/server/ServerReloadableResources;Lnet/minecraft/registry/LayeredRegistryManager;Lnet/minecraft/server/integrated/IntegratedServerLoader$C_tattaqxb;)Lcom/mojang/datafixers/util/Pair;",
			at = @At("HEAD")
	)
	private static void onEndDataPackLoad(AutoCloseableResourceManager resourceManager, ServerReloadableResources resources,
			LayeredRegistryManager<?> layeredRegistryManager, @Coerce Object c_tattaqxb,
			CallbackInfoReturnable<Pair<?, ?>> cir) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, resourceManager, null);
	}

	@ModifyArg(
			method = {"createAndStart", "start(Lnet/minecraft/client/gui/screen/Screen;Ljava/lang/String;ZZ)V"},
			at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Throwable;)V", remap = false),
			index = 1
	)
	private Throwable onFailedDataPackLoad(Throwable throwable) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, null, throwable);
		return throwable; // noop
	}

	@Inject(
			method = "start(Lnet/minecraft/client/gui/screen/Screen;Ljava/lang/String;ZZ)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/integrated/IntegratedServerLoader;askForBackup(Lnet/minecraft/client/gui/screen/Screen;Ljava/lang/String;ZLjava/lang/Runnable;)V"
			),
			locals = LocalCapture.CAPTURE_FAILHARD,
			cancellable = true
	)
	private void onBackupExperimentalWarning(Screen parentScreen, String worldName, boolean safeMode, boolean requireBackup, CallbackInfo ci,
			WorldSaveStorage.Session session, ResourcePackManager resourcePackManager, WorldStem worldStem) {
		if (EXPERIMENTAL_SCREEN_OVERRIDE.toBooleanOrElse(true)
				&& !worldStem.saveProperties().m_ycrrmmel().m_kmrxtmbu()
				&& !FeatureFlags.containsDefault(worldStem.saveProperties().getEnabledFlags())) {
			worldStem.close();
			close(session, worldName);
			this.start(parentScreen, worldName, safeMode, false);
			ci.cancel();
		}
	}

	@Inject(
			method = "tryLoad",
			at = @At(value = "CONSTANT", args = "stringValue=selectWorld.warning.experimental.title"),
			cancellable = true
	)
	private static void onExperimentalWarning(MinecraftClient client, CreateWorldScreen parentScreen,
			Lifecycle dynamicRegistryLifecycle, Runnable successCallback,
			CallbackInfo ci) {
		if (EXPERIMENTAL_SCREEN_OVERRIDE.toBooleanOrElse(true) && ResourceLoaderImpl.EXPERIMENTAL_FEATURES_ENABLED.get() == null) {
			successCallback.run();
			ci.cancel();
		}
	}
}
