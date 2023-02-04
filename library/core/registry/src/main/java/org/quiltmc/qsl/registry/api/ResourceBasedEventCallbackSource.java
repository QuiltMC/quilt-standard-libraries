package org.quiltmc.qsl.registry.api;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.NotNull;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.data.CallbackCodecSource;
import org.quiltmc.qsl.base.api.event.data.CodecAwareCallback;
import org.quiltmc.qsl.base.api.event.data.EventCallbackSource;

public class ResourceBasedEventCallbackSource<T extends CodecAwareCallback> implements EventCallbackSource<T> {
	private static final Gson GSON = new GsonBuilder().setLenient().create();

	final @NotNull Identifier resourcePath;

	final @NotNull CallbackCodecSource<T> codecs;
	final @NotNull Class<T> callbackClass;

	final @NotNull Codec<Pair<Identifier, T>> codec;
	final Map<Identifier, Map<Identifier, T>> listeners = new LinkedHashMap<>();
	final Map<Identifier, Map<Identifier, T>> dynamicListeners = new LinkedHashMap<>();
	Map<Identifier, T[]> listenerArrays = new HashMap<>();

	final Event<T> event;

	final Set<Identifier> registeredEvents = new HashSet<>();
	final Function<Supplier<T[]>, T> combiner;
	final T[] emptyArray;

	public static <T extends CodecAwareCallback> ResourceBasedEventCallbackSource<T> of(
			@NotNull Identifier resourcePath,
			@NotNull CallbackCodecSource<T> codecs,
			@NotNull Class<T> callbackClass,
			@NotNull Event<T> event,
			@NotNull Function<Supplier<T[]>, T> combiner,
			@NotNull ResourceType type) {
		var source = new ResourceBasedEventCallbackSource<>(resourcePath, codecs, callbackClass, event, combiner);
		return source;
	}

	protected ResourceBasedEventCallbackSource(@NotNull Identifier resourcePath, @NotNull CallbackCodecSource<T> codecs, @NotNull Class<T> callbackClass, Event<T> event, Function<Supplier<T[]>, T> combiner) {
		this.resourcePath = resourcePath;
		this.codecs = codecs;
		this.callbackClass = callbackClass;
		this.event = event;
		this.combiner = combiner;
		this.codec = EventCallbackSource.createDelegatingCodec(codecs, callbackClass);

		@SuppressWarnings("unchecked")
		var emptyArray = (T[]) Array.newInstance(callbackClass, 0);
		this.emptyArray = emptyArray;
	}

	public void register(Identifier id, T listener, Identifier phase) {
		listeners.computeIfAbsent(phase, k -> new LinkedHashMap<>()).put(id, listener);
		updateListeners(phase);
	}

	public void register(Identifier id, T listener) {
		register(id, listener, Event.DEFAULT_PHASE);
	}

	private void updateListeners(Identifier phase) {
		var combinedMap = new HashMap<Identifier, T>();
		combinedMap.putAll(listeners.getOrDefault(phase, Map.of()));
		combinedMap.putAll(dynamicListeners.getOrDefault(phase, Map.of()));

		@SuppressWarnings("unchecked")
		var array = (T[]) Array.newInstance(callbackClass, 0);

		this.listenerArrays.put(phase, combinedMap.values().toArray(array));

		if (!registeredEvents.contains(phase)) {
			registeredEvents.add(phase);
			event.register(phase, this.combiner.apply(() -> this.getListeners(phase)));
		}
	}

	private void updateDynamicListeners(Map<Identifier, Map<Identifier, T>> dynamicListeners) {
		this.dynamicListeners.clear();
		for (Map.Entry<Identifier, Map<Identifier, T>> entry : dynamicListeners.entrySet()) {
			this.dynamicListeners.put(entry.getKey(), new LinkedHashMap<>(entry.getValue()));
		}
		for (Identifier phase : dynamicListeners.keySet()) {
			updateListeners(phase);
		}
	}

	private T[] getListeners(Identifier phase) {
		return listenerArrays.getOrDefault(phase, emptyArray);
	}

	public void apply(ResourceManager resourceManager) {
		Map<Identifier, Map<Identifier, T>> dynamicListeners = new LinkedHashMap<>();
		var resources = resourceManager.findResources(resourcePath.getNamespace() + "/" + resourcePath.getPath(), s -> s.getPath().endsWith(".json"));
		for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
			var identifier = entry.getKey();
			identifier = new Identifier(identifier.getNamespace(), identifier.getPath().substring(resourcePath.getNamespace().length() + resourcePath.getPath().length() + 2, identifier.getPath().length() - 5));
			var resource = entry.getValue();
			try (var reader = resource.openBufferedReader()) {
				var json = GSON.fromJson(reader, JsonElement.class);
				DataResult<Pair<Identifier, T>> result = codec.parse(JsonOps.INSTANCE, json);
				if (result.result().isPresent()) {
					var pair = result.result().get();
					dynamicListeners.computeIfAbsent(pair.getFirst(), k -> new LinkedHashMap<>()).put(identifier, pair.getSecond());
				}
				// TODO: proper error handling
			} catch (IOException e) {
				// TODO: proper error handling
			}
		}

		updateDynamicListeners(dynamicListeners);
	}
}
