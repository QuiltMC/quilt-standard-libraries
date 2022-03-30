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

package org.quiltmc.qsl.rendering.registration.impl.client;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;

import org.quiltmc.qsl.rendering.registration.api.client.EntityModelLayerRegistry;
import org.quiltmc.qsl.rendering.registration.mixin.client.EntityModelLayersAccessor;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class EntityModelLayerRegistryImpl {
	private static final Map<EntityModelLayer, EntityModelLayerRegistry.TexturedModelDataProvider> PROVIDERS = new Object2ObjectOpenHashMap<>();

	private EntityModelLayerRegistryImpl() {
	}

	public static void register(EntityModelLayer modelLayer, EntityModelLayerRegistry.TexturedModelDataProvider provider) {
		Objects.requireNonNull(modelLayer, "EntityModelLayer cannot be null");
		Objects.requireNonNull(provider, "TexturedModelDataProvider cannot be null");

		if (PROVIDERS.putIfAbsent(modelLayer, provider) != null) {
			throw new IllegalArgumentException("EntityModelLayer " + modelLayer + " is already mapped to a TexturedModelDataProvider!");
		}

		EntityModelLayersAccessor.getLAYERS().add(modelLayer);
	}

	public static void addModelData(ImmutableMap.Builder<EntityModelLayer, TexturedModelData> builder) {
		PROVIDERS.forEach((layer, provider) -> builder.put(layer, provider.createModelData()));
	}
}
