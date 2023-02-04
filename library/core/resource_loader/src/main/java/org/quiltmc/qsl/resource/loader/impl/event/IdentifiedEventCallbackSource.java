package org.quiltmc.qsl.resource.loader.impl.event;

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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.resource.loader.api.event.CallbackCodecSource;
import org.quiltmc.qsl.resource.loader.api.event.CodecAwareCallback;
import org.quiltmc.qsl.resource.loader.api.event.EventCallbackSource;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;

@ApiStatus.Internal
public class IdentifiedEventCallbackSource<T extends CodecAwareCallback<T>> extends JsonDataLoader implements IdentifiableResourceReloader, EventCallbackSource<T> {
	public static final Gson GSON = new GsonBuilder().setLenient().create();

	final @NotNull Identifier resourcePath;

	final @NotNull CallbackCodecSource<T> codecs;
	final @NotNull Class<T> callbackClass;

	final @NotNull Codec<Pair<Identifier, T>> codec;
	final Map<Identifier, Map<Identifier, T>> listeners = new LinkedHashMap<>();
	final Map<Identifier, Map<Identifier, T>> dynamicListeners = new LinkedHashMap<>();
	Map<Identifier, T[]> listenerArrays;

	final Event<T> event;

	final Set<Identifier> registeredEvents = new HashSet<>();
	final Function<Supplier<T[]>, T> combiner;
	final T[] emptyArray;

	public IdentifiedEventCallbackSource(@NotNull Identifier resourcePath, @NotNull CallbackCodecSource<T> codecs, @NotNull Class<T> callbackClass, Event<T> event, Function<Supplier<T[]>, T> combiner) {
		super(GSON, resourcePath.getNamespace()+"/"+resourcePath.getPath());
		this.resourcePath = resourcePath;
		this.codecs = codecs;
		this.callbackClass = callbackClass;
		this.event = event;
		this.combiner = combiner;
		this.codec = createDelegatingCodec(codecs, callbackClass);

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

	void updateListeners(Identifier phase) {
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

	void updateDynamicListeners(Map<Identifier, Map<Identifier, T>> dynamicListeners) {
		this.dynamicListeners.clear();
		for (Map.Entry<Identifier, Map<Identifier, T>> entry : dynamicListeners.entrySet()) {
			this.dynamicListeners.put(entry.getKey(), new LinkedHashMap<>(entry.getValue()));
		}
		for (Identifier phase : dynamicListeners.keySet()) {
			updateListeners(phase);
		}
	}

	T[] getListeners(Identifier phase) {
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
		}

		updateDynamicListeners(map);
	}

	static <R extends CodecAwareCallback<R>> @NotNull Codec<Pair<Identifier,R>> createDelegatingCodec(@NotNull CallbackCodecSource<R> map, @NotNull Class<R> callbackClass) {
		Codec<R> callbackCodec = Identifier.CODEC.flatXmap(
				identifier ->
						map.lookup(identifier) == null
								? DataResult.<Codec<R>>error("Unregistered "+callbackClass.getSimpleName()+" callback type: " + identifier)
								: DataResult.success(map.lookup(identifier)),
				codec -> {
					Identifier key = map.lookup(codec);
					if (key == null) {
						return DataResult.error("Unregistered "+callbackClass.getSimpleName()+" callback type: " + codec);
					}
					return DataResult.success(key);
				}
		).partialDispatch("type", callback -> {
			var codec = callback.getCodec();
			if (codec == null)
				return DataResult.error("Codec not provided for callback");
			return DataResult.success(codec);
		}, DataResult::success);

		return Codec.pair(Identifier.CODEC.optionalFieldOf("phase", Event.DEFAULT_PHASE).codec(), callbackCodec);
	}
}
