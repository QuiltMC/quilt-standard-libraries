/*
 * Copyright 2021-2022 QuiltMC
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

import com.mojang.serialization.Lifecycle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.class_7196;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.resource.pack.ResourcePackManager;
import net.minecraft.server.WorldStem;
import net.minecraft.unmapped.C_kjxfcecs;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;

import org.quiltmc.qsl.base.api.util.TriState;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

@Mixin(class_7196.class)
public abstract class Class7196Mixin {
	@Shadow
	private static void method_41888(LevelStorage.Session session, String string) {
		throw new IllegalStateException("Mixin injection failed.");
	}

	@Shadow
	protected abstract void method_41899(Screen screen, String string, boolean bl, boolean bl2);

	@Unique
	private static final TriState EXPERIMENTAL_SCREEN_OVERRIDE = TriState.fromProperty("quilt.resource_loader.experimental_screen_override");

	@Inject(method = "method_41900", at = @At("HEAD"))
	private void onStartDataPackLoad(C_kjxfcecs.C_nrmvgbka c_nrmvgbka, C_kjxfcecs.class_6907<SaveProperties> arg, CallbackInfoReturnable<WorldStem> cir) {
		ResourceLoaderEvents.START_DATA_PACK_RELOAD.invoker().onStartDataPackReload(null, null);
	}

	@Inject(method = "method_41900", at = @At("RETURN"))
	private void onEndDataPackLoad(C_kjxfcecs.C_nrmvgbka c_nrmvgbka, C_kjxfcecs.class_6907<SaveProperties> arg, CallbackInfoReturnable<WorldStem> cir) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, cir.getReturnValue().resourceManager(), null);
	}

	@ModifyArg(
			method = {"method_41895", "method_41899"},
			at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Throwable;)V", remap = false),
			index = 1
	)
	private Throwable onFailedDataPackLoad(Throwable throwable) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, null, throwable);
		return throwable; // noop
	}

	@Inject(
			method = "method_41899",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/class_7196;method_41898(Lnet/minecraft/client/gui/screen/Screen;Ljava/lang/String;ZLjava/lang/Runnable;)V"
			),
			locals = LocalCapture.CAPTURE_FAILHARD,
			cancellable = true
	)
	private void onBackupExperimentalWarning(Screen screen, String levelName, boolean b, boolean requireBackup, CallbackInfo ci,
			LevelStorage.Session session, ResourcePackManager resourcePackManager, WorldStem worldStem) {
		if (EXPERIMENTAL_SCREEN_OVERRIDE.toBooleanOrElse(true)
				&& !worldStem.saveProperties().getGeneratorOptions().isLegacyCustomizedType()) {
			worldStem.close();
			method_41888(session, levelName);
			this.method_41899(screen, levelName, b, false);
			ci.cancel();
		}
	}

	@Inject(
			method = "method_41892",
			at = @At(value = "CONSTANT", args = "stringValue=selectWorld.import_worldgen_settings.experimental.title"),
			cancellable = true
	)
	private static void onExperimentalWarning(MinecraftClient minecraftClient, CreateWorldScreen createWorldScreen,
			Lifecycle lifecycle, Runnable runnable,
			CallbackInfo ci) {
		if (EXPERIMENTAL_SCREEN_OVERRIDE.toBooleanOrElse(true)) {
			runnable.run();
			ci.cancel();
		}
	}
}
