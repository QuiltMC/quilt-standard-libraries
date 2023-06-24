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

package org.quiltmc.qsl.command.impl;

import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;

import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.EnumArgumentType;
import org.quiltmc.qsl.command.api.ServerArgumentType;

@ApiStatus.Internal
public final class Initializer implements ModInitializer {
	public static final String NAMESPACE = "quilt_command";

	public static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}

	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public void onInitialize(ModContainer mod) {
		if (QuiltLoader.isModLoaded("quilt_networking")) {
			KnownArgTypesSync.register();
			LOGGER.info("[Quilt Command] Networking support is enabled");
		} else {
			LOGGER.info("[Quilt Command] Networking support is disabled");
		}

		ServerArgumentType.register(id("enum"),
				EnumArgumentType.class,
				new EnumArgumentType.Info(),
				arg -> StringArgumentType.word());
	}
}
