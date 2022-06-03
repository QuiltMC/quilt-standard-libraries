/*
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.registry.impl.event;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.tag.TagKey;
import net.minecraft.util.Holder;
import net.minecraft.util.HolderSet;
import net.minecraft.util.Identifier;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import org.quiltmc.qsl.registry.mixin.SimpleRegistryAccessor;

@ApiStatus.Internal
public final class DelayedRegistry<T> extends MutableRegistry<T> {
	private final MutableRegistry<T> wrapped;
	private final Queue<DelayedEntry<T>> delayedEntries = new LinkedList<>();

	private final @Nullable Function<T, Holder.Reference<T>> customHolderProvider;

	@SuppressWarnings("unchecked")
	DelayedRegistry(MutableRegistry<T> registry) {
		super(registry.getKey(), registry.getLifecycle());

		this.wrapped = registry;

		if (registry instanceof SimpleRegistryAccessor simpleRegistry) {
			this.customHolderProvider = simpleRegistry.getCustomHolderProvider();
		} else {
			this.customHolderProvider = null;
		}
	}

	@Override
	public @Nullable Identifier getId(T entry) {
		return this.wrapped.getId(entry);
	}

	@Override
	public Optional<RegistryKey<T>> getKey(T entry) {
		return this.wrapped.getKey(entry);
	}

	@Override
	public int getRawId(@Nullable T entry) {
		return this.wrapped.getRawId(entry);
	}

	@Override
	public @Nullable T get(int index) {
		return this.wrapped.get(index);
	}

	@Override
	public int size() {
		return this.wrapped.size();
	}

	@Override
	public @Nullable T get(@Nullable RegistryKey<T> key) {
		return this.wrapped.get(key);
	}

	@Override
	public @Nullable T get(@Nullable Identifier id) {
		return this.wrapped.get(id);
	}

	@Override
	public Lifecycle getEntryLifecycle(T entry) {
		return this.wrapped.getEntryLifecycle(entry);
	}

	@Override
	public Lifecycle getLifecycle() {
		return this.wrapped.getLifecycle();
	}

	@Override
	public Set<Identifier> getIds() {
		return this.wrapped.getIds();
	}

	@Override
	public Set<Map.Entry<RegistryKey<T>, T>> getEntries() {
		return this.wrapped.getEntries();
	}

	@Override
	public Set<RegistryKey<T>> method_42021() {
		return this.wrapped.method_42021();
	}

	@Override
	public Optional<Holder<T>> getRandom(RandomGenerator random) {
		return this.wrapped.getRandom(random);
	}

	@Override
	public boolean containsId(Identifier id) {
		return this.wrapped.containsId(id);
	}

	@Override
	public boolean contains(RegistryKey<T> key) {
		return this.wrapped.contains(key);
	}

	@Override
	public Registry<T> freeze() {
		// Refuse freezing.
		return this;
	}

	@Override
	public Holder<T> getOrCreateHolder(RegistryKey<T> key) {
		return this.wrapped.getOrCreateHolder(key);
	}

	@Override
	public Holder.Reference<T> createIntrusiveHolder(T holder) {
		return this.wrapped.createIntrusiveHolder(holder);
	}

	@Override
	public Optional<Holder<T>> getHolder(int index) {
		return this.wrapped.getHolder(index);
	}

	@Override
	public Optional<Holder<T>> getHolder(RegistryKey<T> key) {
		return this.wrapped.getHolder(key);
	}

	@Override
	public Stream<Holder.Reference<T>> holders() {
		return this.wrapped.holders();
	}

	@Override
	public Optional<HolderSet.NamedSet<T>> getTag(TagKey<T> tag) {
		return this.wrapped.getTag(tag);
	}

	@Override
	public HolderSet.NamedSet<T> getOrCreateTag(TagKey<T> key) {
		return this.wrapped.getOrCreateTag(key);
	}

	@Override
	public Stream<Pair<TagKey<T>, HolderSet.NamedSet<T>>> getTags() {
		return this.wrapped.getTags();
	}

	@Override
	public Stream<TagKey<T>> getTagKeys() {
		return this.wrapped.getTagKeys();
	}

	@Override
	public boolean isKnownTag(TagKey<T> tag) {
		return this.wrapped.isKnownTag(tag);
	}

	@Override
	public void resetTags() {
		throw new UnsupportedOperationException("DelayedRegistry does not support resetTags.");
	}

	@Override
	public void bindTags(Map<TagKey<T>, List<Holder<T>>> tags) {
		throw new UnsupportedOperationException("DelayedRegistry does not support bindTags.");
	}

	@Override
	public Iterator<T> iterator() {
		return this.wrapped.iterator();
	}

	@Override
	public Holder<T> set(int rawId, RegistryKey<T> key, T entry, Lifecycle lifecycle) {
		throw new UnsupportedOperationException("DelayedRegistry does not support set.");
	}

	@Override
	public Holder<T> register(RegistryKey<T> key, T entry, Lifecycle lifecycle) {
		this.delayedEntries.add(new DelayedEntry<>(key, entry, lifecycle));

		if (this.customHolderProvider != null) {
			return this.customHolderProvider.apply(entry);
		}

		return new Holder.Direct<>(entry);
	}

	@Override
	public Holder<T> replace(OptionalInt rawId, RegistryKey<T> key, T newEntry, Lifecycle lifecycle) {
		throw new UnsupportedOperationException("DelayedRegistry does not support replacement.");
	}

	@Override
	public boolean isEmpty() {
		return this.wrapped.isEmpty();
	}

	void applyDelayed() {
		DelayedEntry<T> entry;

		while ((entry = this.delayedEntries.poll()) != null) {
			this.wrapped.register(entry.key(), entry.entry(), entry.lifecycle());
		}
	}

	record DelayedEntry<T>(RegistryKey<T> key, T entry, Lifecycle lifecycle) {
	}
}
