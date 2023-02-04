package org.quiltmc.qsl.resource.loader.api.event;

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

import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.data.CallbackCodecSource;
import org.quiltmc.qsl.base.api.event.data.CodecAwareCallback;
import org.quiltmc.qsl.base.api.event.data.EventCallbackSource;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;


public class EventCallbackSourceReloadListener<T extends CodecAwareCallback> extends JsonDataLoader implements IdentifiableResourceReloader, EventCallbackSource<T> {
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

	public static <T extends CodecAwareCallback> EventCallbackSourceReloadListener<T> of(
			@NotNull Identifier resourcePath,
			@NotNull CallbackCodecSource<T> codecs,
			@NotNull Class<T> callbackClass,
			@NotNull Event<T> event,
			@NotNull Function<Supplier<T[]>, T> combiner,
			@NotNull ResourceType type) {
		var source = new EventCallbackSourceReloadListener<>(resourcePath, codecs, callbackClass, event, combiner);
		ResourceLoader.get(type).registerReloader(source);
		return source;
	}

	protected EventCallbackSourceReloadListener(@NotNull Identifier resourcePath, @NotNull CallbackCodecSource<T> codecs, @NotNull Class<T> callbackClass, Event<T> event, Function<Supplier<T[]>, T> combiner) {
		super(GSON, resourcePath.getNamespace()+"/"+resourcePath.getPath());
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

	@Override
	public @NotNull Identifier getQuiltId() {
		return resourcePath;
	}

	@Override
	protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
		Map<Identifier, Map<Identifier, T>> map = new LinkedHashMap<>();

		for (Map.Entry<Identifier, JsonElement> entry : prepared.entrySet()) {
			Identifier id = entry.getKey();
			JsonElement json = entry.getValue();
			DataResult<Pair<Identifier, T>> result = codec.parse(JsonOps.INSTANCE, json);
			if (result.result().isPresent()) {
				Pair<Identifier, T> pair = result.result().get();
				map.computeIfAbsent(pair.getFirst(), k -> new LinkedHashMap<>()).put(id, pair.getSecond());
			}

			// TODO: proper error handling
		}

		updateDynamicListeners(map);
	}
}
