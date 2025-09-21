/*
 * Copyright 2025 The Quilt Project
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

package org.quiltmc.qsl.registry.test;

import static org.quiltmc.qsl.registry.test.RegistryLibBuilderTests.id;

import net.fabricmc.api.EnvType;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.registry.api.QuiltRegistryBuilder;

public class ModRegistries {
	static {
		// horrible no good hack to make sure only the server is aware of the registry
		// (can't just use DedicatedServerModInitializer since that's invoked after registries are frozen)
		if (MinecraftQuiltLoader.getEnvironmentType() != EnvType.SERVER) {
			throw new RuntimeException("Client shouldn't be aware of GAS_TYPE!");
		}
	}

	public static final RegistryKey<Registry<GasType>> GAS_TYPE_KEY = RegistryKey.ofRegistry(id("gas_type"));

	public static final Registry<GasType> GAS_TYPE = QuiltRegistryBuilder.of(GAS_TYPE_KEY)
			.frozen()
			.syncRequired()
			.build();
}
