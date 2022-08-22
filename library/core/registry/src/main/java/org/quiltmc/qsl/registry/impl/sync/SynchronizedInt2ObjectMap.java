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

package org.quiltmc.qsl.registry.impl.sync;

import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.registry.Registry;

/**
 * Represents a map which uses raw registry identifiers as its key.
 * <p>
 * This class wraps an underlying map and make sure to keep it in sync with any registry remapping.
 *
 * @param <V> the value of the map
 * @param <K> the direct registry entry value
 * @author LambdAurora
 */
@ApiStatus.Internal
public class SynchronizedInt2ObjectMap<V, K> implements Int2ObjectMap<V> {
	private final Registry<K> registry;
	private final Reference2ObjectMap<K, V> resilientMap = new Reference2ObjectOpenHashMap<>();
	private final Int2ObjectMap<V> wrapped;

	public SynchronizedInt2ObjectMap(Registry<K> registry, Int2ObjectMap<V> wrapped) {
		this.registry = registry;
		this.wrapped = wrapped;

		if (!this.wrapped.isEmpty()) {
			for (var entry : this.int2ObjectEntrySet()) {
				this.updateResilientMap(entry.getIntKey(), entry.getValue());
			}
		}
	}

	public void rebuildIds() {
		this.wrapped.clear();

		for (var entry : this.resilientMap.entrySet()) {
			this.wrapped.put(this.registry.getRawId(entry.getKey()), entry.getValue());
		}
	}

	public static <V> void attemptRebuildIds(Int2ObjectMap<V> map) {
		if (map instanceof SynchronizedInt2ObjectMap<?, ?> synced) {
			synced.rebuildIds();
		}
	}

	@Override
	public int size() {
		return this.wrapped.size();
	}

	@Override
	public boolean isEmpty() {
		return this.wrapped.isEmpty();
	}

	@Override
	public boolean containsValue(Object value) {
		return this.wrapped.containsValue(value);
	}

	@Override
	public void defaultReturnValue(V v) {
		this.wrapped.defaultReturnValue(v);
	}

	@Override
	public V defaultReturnValue() {
		return this.wrapped.defaultReturnValue();
	}

	@Override
	public ObjectSet<Entry<V>> int2ObjectEntrySet() {
		return this.wrapped.int2ObjectEntrySet();
	}

	@Override
	public IntSet keySet() {
		return this.wrapped.keySet();
	}

	@Override
	public ObjectCollection<V> values() {
		return this.wrapped.values();
	}

	@Override
	public V get(int i) {
		return this.wrapped.get(i);
	}

	@Override
	public boolean containsKey(int i) {
		return this.wrapped.containsKey(i);
	}

	@Override
	public void clear() {
		this.wrapped.clear();
		this.resilientMap.clear();
	}

	private void updateResilientMap(int key, V value) {
		var entry = this.registry.get(key);

		if (entry != null) {
			this.resilientMap.put(entry, value);
		}
	}

	private void onRemoval(int key) {
		var entry = this.registry.get(key);

		if (entry != null) {
			this.resilientMap.remove(entry);
		}
	}

	@Override
	public V put(int key, V value) {
		var result = this.wrapped.put(key, value);
		this.updateResilientMap(key, value);
		return result;
	}

	@Override
	public void putAll(@NotNull Map<? extends Integer, ? extends V> m) {
		this.wrapped.putAll(m);

		for (var entry : m.entrySet()) {
			this.updateResilientMap(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public V remove(int key) {
		V value = this.wrapped.remove(key);
		this.onRemoval(key);
		return value;
	}
}
