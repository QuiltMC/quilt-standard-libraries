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

package org.quiltmc.qsl.registry.impl;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.ResourceManager;

import org.quiltmc.qsl.registry.api.event.DynamicRegistryManagerSetupContext;

/**
 * Represents the context implementation for the {@link org.quiltmc.qsl.registry.api.event.RegistryEvents#DYNAMIC_REGISTRY_SETUP} event.
 * <p>
 * <b>It is imperative that the passed registries are mutable to allow registration.</b>
 *
 * @author LambdAurora
 */
@ApiStatus.Internal
public class DynamicRegistryManagerSetupContextImpl implements DynamicRegistryManagerSetupContext, DynamicRegistryManager {
	private final ResourceManager resourceManager;
	private final Map<RegistryKey<?>, MutableRegistry<?>> registries;

	public DynamicRegistryManagerSetupContextImpl(ResourceManager resourceManager, Stream<MutableRegistry<?>> registries) {
		this.resourceManager = resourceManager;
		this.registries = new Object2ObjectOpenHashMap<>();

		registries.forEach(registry -> this.registries.put(registry.getKey(), registry));
	}

	@Override
	public @NotNull DynamicRegistryManager registryManager() {
		return this;
	}

	@Override
	public @NotNull ResourceManager resourceManager() {
		return this.resourceManager;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public <E> Optional<Registry<E>> getOptional(RegistryKey<? extends Registry<? extends E>> key) {
		return Optional.ofNullable((Registry) this.registries.get(key)).map(registry -> registry);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public Stream<RegistryEntry<?>> registries() {
		return this.registries.entrySet().stream().map(entry -> new RegistryEntry<>((RegistryKey) entry.getKey(), entry.getValue()));
	}
}
