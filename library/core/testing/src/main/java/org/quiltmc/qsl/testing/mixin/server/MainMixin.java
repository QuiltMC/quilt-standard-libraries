/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.testing.mixin.server;

import java.io.File;
import java.nio.file.Path;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.resource.pack.ResourcePackManager;
import net.minecraft.server.Main;
import net.minecraft.server.Services;
import net.minecraft.server.dedicated.EulaReader;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.world.storage.WorldSaveStorage;
import net.minecraft.world.storage.WorldSaveSummary;

import org.quiltmc.loader.api.minecraft.DedicatedServerOnly;
import org.quiltmc.qsl.testing.impl.game.QuiltGameTestImpl;

@DedicatedServerOnly
@Mixin(Main.class)
public class MainMixin {
	@Redirect(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/dedicated/EulaReader;isEulaAgreedTo()Z"))
	private static boolean isEulaAgreedTo(EulaReader reader) {
		return QuiltGameTestImpl.ENABLED || reader.isEulaAgreedTo();
	}

	@Inject(
			method = "main",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resource/pack/VanillaDataPackProvider;createDataPackManager(Ljava/nio/file/Path;)Lnet/minecraft/resource/pack/ResourcePackManager;",
					shift = At.Shift.BY,
					by = 2,
					remap = true
			),
			cancellable = true,
			locals = LocalCapture.CAPTURE_FAILHARD,
			remap = false
	)
	private static void onStart(String[] strings, CallbackInfo ci,
			OptionParser optionParser,
			OptionSpec optionSpec, OptionSpec optionSpec2, OptionSpec optionSpec3, OptionSpec optionSpec4, OptionSpec optionSpec5,
			OptionSpec optionSpec6, OptionSpec optionSpec7, OptionSpec optionSpec8, OptionSpec optionSpec9, OptionSpec optionSpec10,
			OptionSpec optionSpec11, OptionSpec optionSpec12, OptionSpec optionSpec13, OptionSpec optionSpec14, OptionSpec optionSpec15,
			OptionSpec optionSpec16, OptionSet optionSet, Path path, Path path2, ServerPropertiesLoader serverPropertiesLoader,
			Path path3, EulaReader eulaReader, File file, Services services, String string, WorldSaveStorage worldSaveStorage,
			WorldSaveStorage.Session session, WorldSaveSummary worldSaveSummary, boolean bl, ResourcePackManager resourcePackManager) {
		if (QuiltGameTestImpl.ENABLED) {
			QuiltGameTestImpl.runHeadlessServer(session, resourcePackManager);
			ci.cancel(); // Do not progress in starting the normal dedicated server.
		}
	}

	/**
	 * Exits with a non-zero exit code when the test server fails to start,
	 * or {@code gradle test} will succeed without errors otherwise, although no tests have been run.
	 *
	 * @param ci the injection callback info
	 */
	@Inject(
			method = "main",
			at = @At(
					value = "INVOKE",
					target = "Lorg/slf4j/Logger;error(Lorg/slf4j/Marker;Ljava/lang/String;Ljava/lang/Throwable;)V",
					shift = At.Shift.AFTER,
					remap = false
			),
			remap = false
	)
	private static void exitOnError(CallbackInfo ci) {
		if (QuiltGameTestImpl.ENABLED) {
			System.exit(-1);
		}
	}
}
