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

package org.quiltmc.qsl.registry.test;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.util.Holder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.registry.api.QuiltRegistryBuilder;

/**
 * Creates an optional registry with entries holding their own holders.
 */
public class RegistryLibBuilderTest implements ModInitializer {
	private static final String NAMESPACE = "quilt_registry_test_builder";

	public static final Logger LOGGER = LogUtils.getLogger();

	public static final Registry<RegistryEntry> REGISTRY =
			QuiltRegistryBuilder.<RegistryEntry>builtin(new Identifier(NAMESPACE, "registry"))
					.withCustomHolderProvider(RegistryEntry::getRegistryHolder)
					.syncOptional()
					.build();

	@Override
	public void onInitialize(ModContainer mod) {
		var entry = new RegistryEntry();
		LOGGER.info("Check it out! My holder is {}", entry.getRegistryHolder());
		Registry.register(REGISTRY, new Identifier(NAMESPACE, "entry"), entry);
		LOGGER.info("Hey! I got registered! My holder is now {}", entry.getRegistryHolder());
	}

	public static class RegistryEntry {
		private final Holder.Reference<RegistryEntry> registryHolder;

		public RegistryEntry() {
			this.registryHolder = REGISTRY.createIntrusiveHolder(this);
		}

		public Holder.Reference<RegistryEntry> getRegistryHolder() {
			return this.registryHolder;
		}
	}
}
