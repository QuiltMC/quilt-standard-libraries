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

package org.quiltmc.qsl.registry.test.builder;

import com.mojang.logging.LogUtils;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.registry.api.QuiltRegistryBuilder;
import org.slf4j.Logger;

public class RegistryLibBuilderTests implements ModInitializer {
	public static final String NAMESPACE = "quilt_registry_test_builder";

	public static Identifier id(String path) {
		return Identifier.of(NAMESPACE, path);
	}

	public static final Logger LOGGER = LogUtils.getLogger();

	public static final RegistryKey<Registry<GasType>> GAS_TYPE_KEY = RegistryKey.ofRegistry(id("gas_type"));

	public static final Registry<GasType> GAS_TYPE = QuiltRegistryBuilder.of(GAS_TYPE_KEY)
		.withIntrusiveHolders()
		.withSyncRequired()
		.build();

	@Override
	public void onInitialize(ModContainer mod) {
		var oxygen = new GasType();
		LOGGER.info("Oxygen GasType holder is {}", oxygen.getRegistryHolder());
		Registry.register(GAS_TYPE, id("oxygen"), oxygen);
		LOGGER.info("Registered! Oxygen GasType holder is {}", oxygen.getRegistryHolder());
	}

}
