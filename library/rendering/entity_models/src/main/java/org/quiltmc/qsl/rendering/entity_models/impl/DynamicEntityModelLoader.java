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

package org.quiltmc.qsl.rendering.entity_models.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.quiltmc.qsl.rendering.entity_models.api.Codecs;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleResourceReloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

public class DynamicEntityModelLoader implements SimpleResourceReloader<DynamicEntityModelLoader.ModelLoader> {
	private static final Logger LOGGER = LoggerFactory.getLogger("Quilt Entity Model Manager");
	private Map<EntityModelLayer, TexturedModelData> modelData;

	public TexturedModelData getModelData(EntityModelLayer layer) {
		return modelData.get(layer);
	}

	@Override
	public CompletableFuture<ModelLoader> load(ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.supplyAsync(() -> new ModelLoader(manager, profiler), executor);
	}

	@Override
	public CompletableFuture<Void> apply(ModelLoader prepared, ResourceManager manager, Profiler profiler, Executor executor) {
		this.modelData = prepared.getModelData();
		return CompletableFuture.runAsync(() -> {
		});
	}

	@Override
	public Identifier getQuiltId() {
		return new Identifier("quilt_entity_models", "entity_model_reloader");
	}

	public static class ModelLoader {
		private static final Pattern PATH_AND_NAME_PATTERN = Pattern.compile("models\\/entity\\/((\\w|\\/)*)\\/(\\w*)\\.json");

		private final ResourceManager manager;
		private final Profiler profiler;
		private final Map<EntityModelLayer, TexturedModelData> modelData = new HashMap<>();

		public ModelLoader(ResourceManager manager, Profiler profiler) {
			this.manager = manager;
			this.profiler = profiler;
			loadAnimations();
		}

		private void loadAnimations() {
			profiler.push("Load Entity Models");
			Map<Identifier, Resource> resources = manager.findResources("models/entity", id -> id.getPath().endsWith(".json"));
			for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
				addModel(entry.getKey(), entry.getValue());
			}
			profiler.pop();
		}

		private void addModel(Identifier id, Resource resource) {
			BufferedReader reader;
			try {
				reader = resource.openBufferedReader();
			} catch (IOException e) {
				LOGGER.error(String.format("Unable to open BufferedReader for id %s", id), e);
				return;
			}

			JsonObject json = JsonHelper.deserialize(reader);
			DataResult<Pair<TexturedModelData, JsonElement>> result = Codecs.Model.TEXTURED_MODEL_DATA.decode(JsonOps.INSTANCE, json);

			if (result.error().isPresent()) {
				LOGGER.error(String.format("Unable to parse entity model file %s.\nReason: %s", id, result.error().get().message()));
				return;
			}

			Matcher matcher = PATH_AND_NAME_PATTERN.matcher(id.getPath());
			if (!matcher.find()) {
				LOGGER.error(String.format("Unable create model layer for entity model file %s.", id));
				return;
			}

			String path = matcher.group(1);
			String name = matcher.group(2);

			Identifier modelID = new Identifier(id.getNamespace(), path);
			modelData.put(new EntityModelLayer(modelID, name), result.result().get().getFirst());
		}

		public Map<EntityModelLayer, TexturedModelData> getModelData() {
			return modelData;
		}
	}
}
