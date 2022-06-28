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
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleResourceReloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.render.animation.Animation;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;

public class AnimationManager implements SimpleResourceReloader<AnimationManager.AnimationLoader> {
	private static final Logger LOGGER = LoggerFactory.getLogger("Quilt Animation Manager");
	private Map<Identifier, Animation> animations;

	public Animation getAnimation(Identifier id) {
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
	public Identifier getQuiltId() {
		return new Identifier("quilt_entity_models", "animation_reloader");
	}

	public static class AnimationLoader {
		private final ResourceManager manager;
		private final Profiler profiler;
		private final Map<Identifier, Animation> animations = new HashMap<>();

		public AnimationLoader(ResourceManager manager, Profiler profiler) {
			this.manager = manager;
			this.profiler = profiler;
			loadAnimations();
		}

		private void loadAnimations() {
			profiler.push("Load Animations");
			Map<Identifier, Resource> resources = manager.findResources("animations", id -> id.getPath().endsWith(".json"));
			for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
				addAnimation(entry.getKey(), entry.getValue());
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
			DataResult<Pair<Animation, JsonElement>> result = Codecs.ANIMATION.decode(JsonOps.INSTANCE, json);

			if (result.error().isPresent()) {
				LOGGER.error(String.format("Unable to parse animation file %s.\nReason: %s", id, result.error().get().message()));
				return;
			}

			animations.put(new Identifier(id.getNamespace(), id.getPath().substring("animations/".length())), result.result().get().getFirst());
		}

		public Map<Identifier, Animation> getAnimations() {
			return animations;
		}
	}
}
