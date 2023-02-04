package org.quiltmc.qsl.base.api.event.data;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
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
 * Provides a single callback for an event that collects both callbacks with identifiers provided in code and callbacks
 * provided by datapacks. Additionally, contains logic to load callbacks from a resource manager. This tool keeps a
 * separate map of callbacks for each event phase, so the same identifier can be used for different callbacks in different
 * phases.
 * @param <T> the type of the event callback
 */
public class DynamicEventCallbackSource<T extends CodecAware> {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Gson GSON = new GsonBuilder().setLenient().create();

	protected final @NotNull Identifier resourcePath;

	protected final @NotNull CodecMap<T> codecs;
	protected final @NotNull Class<T> callbackClass;

	private final @NotNull Codec<Pair<List<Identifier>, T>> codec;
	private final Map<Identifier, Map<Identifier, T>> listeners = new HashMap<>();
	private final Map<Identifier, Map<Identifier, T>> dynamicListeners = new HashMap<>();
	private final Map<Identifier, T[]> listenerArrays = new HashMap<>();

	protected final Event<T> event;

	private final Set<Identifier> registeredEvents = new HashSet<>();
	protected final Function<Supplier<T[]>, T> combiner;
	private final T[] emptyArray;

	/**
	 * Creates a new callback source that listens to a given event and loads from a provided resource path. For instance,
	 * if the resource path is {@code "quilt:my_callbacks"}, then callbacks will be loaded under the identifier
	 * {@code "<namespace>:<path>"} from {@code "<namespace>/quilt/my_callbacks/<path>.json"}.
	 * @param resourcePath the path to the resource directory containing callbacks
	 * @param codecs delegates codecs to decode callbacks with
	 * @param callbackClass the class of the event callback
	 * @param event the event to listen to
	 * @param combiner a function for combining multiple callbacks registered to this and loaded from data to a single callback
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
	 * Listens to the event in a way that data can replace.
	 * @param id the identifier of the callback, to be used when replacing it in data
	 * @param listener the callback to listen with
	 * @param phase the phase to register the callback in
	 */
	public void register(Identifier id, T listener, Identifier phase) {
		listeners.computeIfAbsent(phase, k -> new LinkedHashMap<>()).put(id, listener);
		updateListeners(phase);
	}

	/**
	 * Listens to the event in a way that data can replace.
	 * @param id the identifier of the callback, to be used when replacing it in data
	 * @param listener the callback to listen with
	 */
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

	/**
	 * Updates the listeners with callbacks loaded from data.
	 * @param resourceManager the resource manager to load data from
	 */
	public void update(ResourceManager resourceManager) {
		this.update(resourceManager, JsonOps.INSTANCE);
	}

	/**
	 * Updates the listeners with callbacks loaded from data, using custom {@link DynamicOps}. This is useful when
	 * loading using codecs that want a {@link net.minecraft.registry.RegistryOps} or similar.
	 * @param resourceManager the resource manager to load data from
	 * @param ops the dynamic ops to use to decode data
	 */
	public void update(ResourceManager resourceManager, DynamicOps<JsonElement> ops) {
		Map<Identifier, Map<Identifier, T>> dynamicListeners = new LinkedHashMap<>();
		ResourceFileNamespace resourceFileNamespace = ResourceFileNamespace.json(this.resourcePath.getNamespace()+"/"+this.resourcePath.getPath());
		var resources = resourceFileNamespace.findMatchingResources(resourceManager).entrySet();
		for (Map.Entry<Identifier, Resource> entry : resources) {
			Identifier identifier = entry.getKey();
			Identifier unwrappedIdentifier = resourceFileNamespace.unwrapFilePath(identifier);
			var resource = entry.getValue();
			try (var reader = resource.openBufferedReader()) {
				var json = GSON.fromJson(reader, JsonElement.class);
				DataResult<Pair<List<Identifier>, T>> result = codec.parse(ops, json);
				if (result.result().isPresent()) {
					var pair = result.result().get();
					for (Identifier phase : pair.getFirst()) {
						dynamicListeners.computeIfAbsent(phase, k -> new LinkedHashMap<>()).put(unwrappedIdentifier, pair.getSecond());
					}
				} else {
					LOGGER.error("Couldn't parse data file {} from {}: {}", unwrappedIdentifier, identifier, result.error().get().message());
				}
			} catch (IOException e) {
				LOGGER.error("Couldn't parse data file {} from {}", unwrappedIdentifier, identifier, e);
			}
		}

		updateDynamicListeners(dynamicListeners);
	}

	/**
	 * {@return the codec used to decode resources; can be used to re-encode callbacks}
	 */
	public @NotNull Codec<Pair<List<Identifier>, T>> getCodec() {
		return codec;
	}
}
