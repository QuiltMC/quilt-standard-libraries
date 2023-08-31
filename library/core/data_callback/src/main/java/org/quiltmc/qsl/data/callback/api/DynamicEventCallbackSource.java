/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.data.callback.api;

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
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import net.minecraft.registry.ResourceFileNamespace;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import org.quiltmc.qsl.base.api.event.Event;

/**
 * Provides a single callback per phase for an event that collects both callbacks with identifiers provided in code and
 * callbacks provided by datapacks. Additionally, contains logic to load callbacks from a resource manager. This tool
 * keeps information about event phases alongside registered callbacks, so that a given identifier uniquely identifies
 * a callback even across phases.
 *
 * @param <T> the type of the event callback
 */
public class DynamicEventCallbackSource<T extends CodecAware> {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Gson GSON = new GsonBuilder().setLenient().create();

	protected final @NotNull Identifier resourcePath;

	protected final @NotNull CodecMap<T> codecs;
	protected final @NotNull Class<T> callbackClass;
	protected final Event<T> event;
	protected final Function<Supplier<T[]>, T> combiner;
	private final @NotNull Codec<Pair<Identifier, T>> codec;
	private final Map<Identifier, Pair<Identifier, T>> listeners = new LinkedHashMap<>();
	private final Map<Identifier, Pair<Identifier, T>> dynamicListeners = new LinkedHashMap<>();
	private final Map<Identifier, T[]> listenerArrays = new HashMap<>();
	private final Set<Identifier> registeredEvents = new HashSet<>();
	private final T[] emptyArray;

	/**
	 * Creates a new callback source that listens to a given event and loads from a provided resource path. For instance,
	 * if the resource path is {@code "quilt:my_callbacks"}, then callbacks will be loaded under the identifier
	 * {@code "<namespace>:<path>"} from {@code "<namespace>/quilt/my_callbacks/<path>.json"}.
	 *
	 * @param resourcePath  the path to the resource directory containing callbacks
	 * @param codecs        delegates codecs to decode callbacks with
	 * @param callbackClass the class of the event callback
	 * @param event         the event to listen to
	 * @param combiner      a function for combining multiple callbacks registered to this and loaded from data to a single callback
	 */
	public DynamicEventCallbackSource(@NotNull Identifier resourcePath, @NotNull CodecMap<T> codecs, @NotNull Class<T> callbackClass, Event<T> event, Function<Supplier<T[]>, T> combiner) {
		this.resourcePath = resourcePath;
		this.codecs = codecs;
		this.callbackClass = callbackClass;
		this.event = event;
		this.combiner = combiner;
		this.codec = Codecs.createLazy(() -> codecs.createDelegatingCodecPhased(callbackClass.getSimpleName()));

		@SuppressWarnings("unchecked")
		var emptyArray = (T[]) Array.newInstance(callbackClass, 0);
		this.emptyArray = emptyArray;
	}

	/**
	 * Listens to the event in a way that data can replace. A given identifier can only have one callback associated
	 * with it for any given callback source.
	 *
	 * @param id       the identifier of the callback, to be used when replacing it in data
	 * @param listener the callback to listen with
	 * @param phase    the phase to register the callback in
	 */
	public void register(Identifier id, T listener, Identifier phase) {
		this.listeners.put(id, Pair.of(phase, listener));
		this.updateListeners(phase);
	}

	/**
	 * Listens to the event in a way that data can replace.
	 *
	 * @param id       the identifier of the callback, to be used when replacing it in data
	 * @param listener the callback to listen with
	 */
	public void register(Identifier id, T listener) {
		this.register(id, listener, Event.DEFAULT_PHASE);
	}

	private void updateListeners(Identifier phase) {
		var combinedMap = new TreeMap<Identifier, T>();

		for (var entry : this.listeners.entrySet()) {
			if (entry.getValue().getFirst().equals(phase)) {
				combinedMap.put(entry.getKey(), entry.getValue().getSecond());
			}
		}

		for (var entry : this.dynamicListeners.entrySet()) {
			if (entry.getValue().getFirst().equals(phase)) {
				combinedMap.put(entry.getKey(), entry.getValue().getSecond());
			}
		}

		@SuppressWarnings("unchecked")
		var array = (T[]) Array.newInstance(this.callbackClass, combinedMap.size());

		int i = 0;
		for (T t : combinedMap.values()) {
			array[i] = t;
			i++;
		}

		this.listenerArrays.put(phase, array);

		if (!this.registeredEvents.contains(phase)) {
			this.registeredEvents.add(phase);
			this.event.register(phase, this.combiner.apply(() -> this.getListeners(phase)));
		}
	}

	private void updateDynamicListeners(Map<Identifier, Pair<Identifier, T>> dynamicListeners) {
		this.dynamicListeners.clear();
		this.dynamicListeners.putAll(dynamicListeners);
		dynamicListeners.values().stream()
				.map(Pair::getFirst)
				.distinct()
				.forEach(this::updateListeners);
	}

	private T[] getListeners(Identifier phase) {
		return this.listenerArrays.getOrDefault(phase, this.emptyArray);
	}

	/**
	 * Updates the listeners with callbacks loaded from data.
	 *
	 * @param resourceManager the resource manager to load data from
	 */
	public void update(ResourceManager resourceManager) {
		this.update(resourceManager, JsonOps.INSTANCE);
	}

	/**
	 * Updates the listeners with callbacks loaded from data, using custom {@link DynamicOps}. This is useful when
	 * loading using codecs that want a {@link net.minecraft.registry.RegistryOps} or similar.
	 *
	 * @param resourceManager the resource manager to load data from
	 * @param ops             the dynamic ops to use to decode data
	 */
	public void update(ResourceManager resourceManager, DynamicOps<JsonElement> ops) {
		var dynamicListeners = new LinkedHashMap<Identifier, Pair<Identifier, T>>();
		ResourceFileNamespace resourceFileNamespace = ResourceFileNamespace.json(this.resourcePath.getNamespace() + "/" + this.resourcePath.getPath());

		var resources = resourceFileNamespace.findMatchingResources(resourceManager).entrySet();
		for (Map.Entry<Identifier, Resource> entry : resources) {
			Identifier id = entry.getKey();
			Identifier unwrappedIdentifier = resourceFileNamespace.unwrapFilePath(id);

			var resource = entry.getValue();
			try (var reader = resource.openBufferedReader()) {
				var json = GSON.fromJson(reader, JsonElement.class);
				DataResult<Pair<Identifier, T>> result = this.codec.parse(ops, json);

				if (result.result().isPresent()) {
					var pair = result.result().get();
					dynamicListeners.put(unwrappedIdentifier, pair);
				} else {
					LOGGER.error("Couldn't parse data file {} from {}: {}", unwrappedIdentifier, id, result.error().get().message());
				}
			} catch (IOException e) {
				LOGGER.error("Couldn't parse data file {} from {}", unwrappedIdentifier, id, e);
			}
		}

		this.updateDynamicListeners(dynamicListeners);
	}

	/**
	 * {@return the codec used to decode resources; can be used to re-encode callbacks}
	 */
	public @NotNull Codec<Pair<Identifier, T>> getCodec() {
		return this.codec;
	}
}
