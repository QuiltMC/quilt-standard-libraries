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

package org.quiltmc.qsl.base.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.Main;

import org.quiltmc.loader.api.entrypoint.EntrypointUtil;
import org.quiltmc.loader.api.minecraft.DedicatedServerOnly;
import org.quiltmc.qsl.base.api.entrypoint.server.DedicatedServerModInitializer;

@DedicatedServerOnly
@Mixin(Main.class)
public class MainMixin {
	@Inject(
			method = "main",
			at = @At(value = "INVOKE", target = "Ljava/io/File;<init>(Ljava/lang/String;)V", ordinal = 0),
			remap = false
	)
	private static void onInit(String[] strings, CallbackInfo ci) {
		EntrypointUtil.invoke(
				DedicatedServerModInitializer.ENTRYPOINT_KEY, DedicatedServerModInitializer.class, DedicatedServerModInitializer::onInitializeServer
		);
	}
}
