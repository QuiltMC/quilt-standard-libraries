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

package org.quiltmc.qsl.rendering.registration.api.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;

import org.quiltmc.qsl.rendering.registration.impl.client.EntityModelLayerRegistryImpl;

/**
 * Allows registering {@linkplain EntityModelLayer}s and mapping them to {@linkplain TexturedModelData}.
 */
@Environment(EnvType.CLIENT)
public final class EntityModelLayerRegistry {
	private EntityModelLayerRegistry() {
	}

	/**
	 * Registers an {@linkplain EntityModelLayer} and maps it to a provider for a {@linkplain TexturedModelData}.
	 *
	 * @param modelLayer the entity model layer
	 * @param provider the provider for the textured model data
	 */
	public static void register(EntityModelLayer modelLayer, TexturedModelDataProvider provider) {
		EntityModelLayerRegistryImpl.register(modelLayer, provider);
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface TexturedModelDataProvider {
		/**
		 * Creates the textured model data for use in a {@link EntityModelLayer}.
		 *
		 * @return the textured model data for the entity model layer.
		 */
		TexturedModelData createModelData();
	}
}
