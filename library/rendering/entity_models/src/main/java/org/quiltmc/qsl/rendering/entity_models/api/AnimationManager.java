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

package org.quiltmc.qsl.rendering.entity_models.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleResourceReloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.render.animation.Animation;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

/**
 * A class that loads and holds {@link Animation}s.
 *
 * See {@link net.minecraft.client.render.entity.model.EntityModelLoader#getModelPart(EntityModelLayer)} for a similar usage.
 */
public class AnimationManager implements SimpleResourceReloader<AnimationManager.AnimationLoader> {
	private static final Logger LOGGER = LoggerFactory.getLogger("Quilt Animation Manager");
	private Map<Identifier, Animation> animations;

	/**
	 *
	 * @param id The animation ID
	 * @return An animation if found, or null otherwise
	 */
	public @Nullable Animation getAnimation(Identifier id) {
		return animations.get(id);
	}

	@Override
	public CompletableFuture<AnimationLoader> load(ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.supplyAsync(() -> new AnimationLoader(manager, profiler), executor);
	}

	@Override
	public CompletableFuture<Void> apply(AnimationLoader prepared, ResourceManager manager, Profiler profiler, Executor executor) {
		this.animations = prepared.getAnimations();
		return CompletableFuture.runAsync(() -> {
		});
	}

	@Override
	public @NotNull Identifier getQuiltId() {
		return new Identifier("quilt_entity_models", "animation_reloader");
	}

	public static class AnimationLoader {
		private final ResourceManager manager;
		private final Profiler profiler;
		private final Map<Identifier, Animation> animations = new HashMap<>();

		public AnimationLoader(ResourceManager manager, Profiler profiler) {
			this.manager = manager;
			this.profiler = profiler;
			this.loadAnimations();
		}

		private void loadAnimations() {
			profiler.push("Load Animations");
			Map<Identifier, Resource> resources = manager.findResources("animations", id -> id.getPath().endsWith(".json"));
			for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
				this.addAnimation(entry.getKey(), entry.getValue());
			}
			profiler.pop();
		}

		private void addAnimation(Identifier id, Resource resource) {
			BufferedReader reader;
			try {
				reader = resource.openBufferedReader();
			} catch (IOException e) {
				LOGGER.error(String.format("Unable to open BufferedReader for id %s", id), e);
				return;
			}

			JsonObject json = JsonHelper.deserialize(reader);
			DataResult<Pair<Animation, JsonElement>> result = Codecs.Animations.ANIMATION.decode(JsonOps.INSTANCE, json);

			if (result.error().isPresent()) {
				LOGGER.error(String.format("Unable to parse animation file %s.\nReason: %s", id, result.error().get().message()));
				return;
			}

			Identifier animationId = new Identifier(id.getNamespace(), id.getPath().substring("animations/".length()));
			animations.put(animationId, result.result().get().getFirst());
		}

		public Map<Identifier, Animation> getAnimations() {
			return animations;
		}
	}
}
