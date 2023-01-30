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

package org.quiltmc.qsl.registry.test.builder;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.registry.api.QuiltRegistryBuilder;
import org.quiltmc.qsl.registry.api.RegistrySyncBehavior;

public class RegistryLibBuiltinBuilderTest implements ModInitializer {
	public static final String NAMESPACE = "quilt_registry_test_builtin_builder";

	public static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}

	public static final Logger LOGGER = LogUtils.getLogger();

	public static final RegistryKey<Registry<GasType>> GAS_KEY = RegistryKey.ofRegistry(id("gas"));

	public static final Registry<GasType> GAS = QuiltRegistryBuilder.builtin(GAS_KEY)
			.withSyncBehavior(RegistrySyncBehavior.REQUIRED)
			.withCustomHolderProvider(GasType::getRegistryHolder)
			.build();

	@Override
	public void onInitialize(ModContainer mod) {
		var oxygen = new GasType();
		LOGGER.info("Oxygen GasType holder is " + oxygen.getRegistryHolder());
		Registry.register(GAS, id("oxygen"), oxygen);
		LOGGER.info("Registered! Oxygen GasType holder is " + oxygen.getRegistryHolder());
	}
}
