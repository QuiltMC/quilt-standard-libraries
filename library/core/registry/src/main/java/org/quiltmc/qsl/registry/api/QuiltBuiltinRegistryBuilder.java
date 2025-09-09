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

package org.quiltmc.qsl.registry.api;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.registry.api.sync.RegistrySynchronization;

/**
 * Utility class to build a new built-in {@link Registry}.
 *
 * @param <T> the entry type tracked by this registry
 */
public final class QuiltBuiltinRegistryBuilder<T> extends QuiltRegistryBuilder<T, QuiltBuiltinRegistryBuilder<T>> {
	QuiltBuiltinRegistryBuilder(@NotNull RegistryKey<Registry<T>> key) {
		super(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void onRegistryBuilt(SimpleRegistry<T> registry) {
		Registry.register((Registry<Registry<Object>>) Registries.ROOT, this.key.getValue(), (Registry<Object>) registry);

		if (this.syncBehavior == RegistrySynchronizationBehavior.REQUIRED || this.syncBehavior == RegistrySynchronizationBehavior.OPTIONAL) {
			RegistrySynchronization.markForSync(registry);

			if (this.syncBehavior == RegistrySynchronizationBehavior.OPTIONAL) {
				RegistrySynchronization.setRegistryOptional(registry);
			}
		}

		super.onRegistryBuilt(registry);
	}
}
