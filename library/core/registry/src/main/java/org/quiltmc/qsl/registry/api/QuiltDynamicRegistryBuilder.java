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

package org.quiltmc.qsl.registry.api;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;

import org.quiltmc.qsl.registry.impl.SignalingMemoizingSupplier;

/**
 * Utility class to build a new dynamic {@link Registry}.
 * <p>
 * Dynamic registries can receive serialized entries from datapacks (and as such are unique to every world),
 * and can also be synchronized automatically - even possibly using a different (optimized) codec for it!
 *
 * @param <T> the entry type tracked by this registry
 */
public final class QuiltDynamicRegistryBuilder<T> extends QuiltRegistryBuilder<T, QuiltDynamicRegistryBuilder<T>> {
	private final Codec<T> entryCodec, networkEntryCodec;

	QuiltDynamicRegistryBuilder(@NotNull Identifier id, @NotNull Codec<T> entryCodec, @Nullable Codec<T> networkEntryCodec) {
		super(id);

		this.entryCodec = entryCodec;
		this.networkEntryCodec = networkEntryCodec;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onRegistryBuilt(SimpleRegistry<T> registry) {
		if (DynamicRegistryManager.BUILTIN instanceof SignalingMemoizingSupplier<DynamicRegistryManager.Frozen> supplier) {
			if (supplier.isInitialized()) {
				throw new IllegalStateException("Built-in DynamicRegistryManager was already initialized!");
			}
		}

		BuiltinRegistries.register((Registry<Registry<Object>>) BuiltinRegistries.REGISTRIES, this.key.getValue(), (Registry<Object>) registry);

		var infos = DynamicRegistryManager.INFOS;

		infos.put(this.key, new DynamicRegistryManager.Info<>(this.key, this.entryCodec, this.networkEntryCodec));

		super.onRegistryBuilt(registry);
	}
}
