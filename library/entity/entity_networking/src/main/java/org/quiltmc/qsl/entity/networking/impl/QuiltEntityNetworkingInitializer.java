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

package org.quiltmc.qsl.entity.networking.impl;

import com.mojang.serialization.Lifecycle;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.registry.api.sync.RegistrySynchronization;

@ApiStatus.Internal
public class QuiltEntityNetworkingInitializer implements ModInitializer {
	public static final Identifier EXTENDED_SPAWN_PACKET_ID = new Identifier("quilt", "extended_entity_spawn_packet");

	public static final SimpleRegistry<TrackedDataHandler<?>> TRACKED_DATA_HANDLER_REGISTRY = new SimpleRegistry<>(
			RegistryKey.ofRegistry(new Identifier("quilt", "tracked_data_handlers")), Lifecycle.stable(), false
	);

	private static boolean markForSync = true;

	public static <T> TrackedDataHandler<T> register(Identifier identifier, TrackedDataHandler<T> handler) {
		Registry.register(QuiltEntityNetworkingInitializer.TRACKED_DATA_HANDLER_REGISTRY, identifier, handler);

		if (markForSync) {
			RegistrySynchronization.markForSync(QuiltEntityNetworkingInitializer.TRACKED_DATA_HANDLER_REGISTRY);
			markForSync = false;
		}

		return handler;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(((Registry<Registry<TrackedDataHandler<?>>>) Registries.REGISTRY), TRACKED_DATA_HANDLER_REGISTRY.getKey().getValue(), TRACKED_DATA_HANDLER_REGISTRY);
	}
}
