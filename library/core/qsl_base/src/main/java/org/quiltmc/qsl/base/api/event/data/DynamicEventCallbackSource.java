package org.quiltmc.qsl.base.api.event.data;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import net.minecraft.registry.ResourceFileNamespace;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import org.quiltmc.qsl.base.api.event.Event;

public class DynamicEventCallbackSource<T extends CodecAwareCallback> {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Gson GSON = new GsonBuilder().setLenient().create();

	protected final @NotNull Identifier resourcePath;

	protected final @NotNull CodecMap<T> codecs;
	protected final @NotNull Class<T> callbackClass;

	private final @NotNull Codec<Pair<Identifier, T>> codec;
	private final Map<Identifier, Map<Identifier, T>> listeners = new HashMap<>();
	private final Map<Identifier, Map<Identifier, T>> dynamicListeners = new HashMap<>();
	private final Map<Identifier, T[]> listenerArrays = new HashMap<>();

	protected final Event<T> event;

	private final Set<Identifier> registeredEvents = new HashSet<>();
	protected final Function<Supplier<T[]>, T> combiner;
	private final T[] emptyArray;

	public DynamicEventCallbackSource(@NotNull Identifier resourcePath, @NotNull CodecMap<T> codecs, @NotNull Class<T> callbackClass, Event<T> event, Function<Supplier<T[]>, T> combiner) {
		this.resourcePath = resourcePath;
		this.codecs = codecs;
		this.callbackClass = callbackClass;
		this.event = event;
		this.combiner = combiner;
		this.codec = Codecs.createLazy(() -> CodecMap.createDelegatingCodecPhased(codecs, callbackClass));

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
		var combinedMap = new TreeMap<Identifier, T>();
		combinedMap.putAll(listeners.getOrDefault(phase, Map.of()));
		combinedMap.putAll(dynamicListeners.getOrDefault(phase, Map.of()));

		@SuppressWarnings("unchecked")
		var array = (T[]) Array.newInstance(callbackClass, combinedMap.size());

		int i = 0;
		for (T t : combinedMap.values()) {
			array[i] = t;
			i++;
		}

		this.listenerArrays.put(phase, array);

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
		ResourceFileNamespace resourceFileNamespace = ResourceFileNamespace.json(this.resourcePath.getNamespace()+"/"+this.resourcePath.getPath());
		var resources = resourceFileNamespace.findMatchingResources(resourceManager).entrySet();
		for (Map.Entry<Identifier, Resource> entry : resources) {
			Identifier identifier = entry.getKey();
			Identifier unwrappedIdentifier = resourceFileNamespace.unwrapFilePath(identifier);
			var resource = entry.getValue();
			try (var reader = resource.openBufferedReader()) {
				var json = GSON.fromJson(reader, JsonElement.class);
				DataResult<Pair<Identifier, T>> result = codec.parse(JsonOps.INSTANCE, json);
				if (result.result().isPresent()) {
					var pair = result.result().get();
					dynamicListeners.computeIfAbsent(pair.getFirst(), k -> new LinkedHashMap<>()).put(unwrappedIdentifier, pair.getSecond());
				} else {
					LOGGER.error("Couldn't parse data file {} from {}: {}", unwrappedIdentifier, identifier, result.error().get().message());
				}
			} catch (IOException e) {
				LOGGER.error("Couldn't parse data file {} from {}", unwrappedIdentifier, identifier, e);
			}
		}

		updateDynamicListeners(dynamicListeners);
	}

	public @NotNull Codec<Pair<Identifier, T>> getCodec() {
		return codec;
	}
}
