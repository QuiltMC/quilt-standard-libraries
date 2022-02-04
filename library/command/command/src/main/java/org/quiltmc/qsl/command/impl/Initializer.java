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

package org.quiltmc.qsl.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.command.api.EnumArgumentType;
import org.quiltmc.qsl.command.api.ServerArgumentType;

@ApiStatus.Internal
public final class Initializer implements ModInitializer {
	public static final String NAMESPACE = "quilt_command";

	public static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}

	@Override
	public void onInitialize() {
		if (FabricLoader.getInstance().isModLoaded("quilt_networking")) {
			KnownArgumentTypesSync.register();
		}

		ServerArgumentType.register(id("enum"),
				EnumArgumentType.class,
				new EnumArgumentType.Serializer(),
				arg -> StringArgumentType.word());
	}
}
