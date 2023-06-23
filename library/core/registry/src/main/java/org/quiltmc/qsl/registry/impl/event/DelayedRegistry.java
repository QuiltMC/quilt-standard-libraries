/*
 * Copyright 2021 The Quilt Project
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
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.registry.Holder;
import net.minecraft.registry.HolderOwner;
import net.minecraft.registry.HolderProvider;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.Holder.Reference;
import net.minecraft.registry.HolderLookup.RegistryLookup;
import net.minecraft.registry.HolderSet.NamedSet;
import net.minecraft.registry.tag.TagKey;

@ApiStatus.Internal
public final class DelayedRegistry<T> implements MutableRegistry<T> {
	private final MutableRegistry<T> wrapped;
	private final Queue<DelayedEntry<T>> delayedEntries = new LinkedList<>();

	DelayedRegistry(MutableRegistry<T> registry) {
		this.wrapped = registry;
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
	public @Nullable T get(@Nullable RegistryKey<T> entry) {
		return this.wrapped.get(entry);
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
	public Set<Entry<RegistryKey<T>, T>> getEntries() {
		return this.wrapped.getEntries();
	}

	@Override
	public Set<RegistryKey<T>> getKeys() {
		return this.wrapped.getKeys();
	}

	@Override
	public Optional<Reference<T>> getRandom(RandomGenerator random) {
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
	public Reference<T> createIntrusiveHolder(T holder) {
		return this.wrapped.createIntrusiveHolder(holder);
	}

	@Override
	public Optional<Reference<T>> getHolder(int index) {
		return this.wrapped.getHolder(index);
	}

	@Override
	public Optional<Reference<T>> getHolder(RegistryKey<T> key) {
		return this.wrapped.getHolder(key);
	}

	@Override
	public Holder<T> wrapAsHolder(T object) {
		return this.wrapped.wrapAsHolder(object);
	}

	@Override
	public Stream<Reference<T>> holders() {
		return this.wrapped.holders();
	}

	@Override
	public Optional<NamedSet<T>> getTag(TagKey<T> tag) {
		return this.wrapped.getTag(tag);
	}

	@Override
	public NamedSet<T> getOrCreateTag(TagKey<T> key) {
		return this.wrapped.getOrCreateTag(key);
	}

	@Override
	public Stream<Pair<TagKey<T>, NamedSet<T>>> getTags() {
		return this.wrapped.getTags();
	}

	@Override
	public Stream<TagKey<T>> getTagKeys() {
		return this.wrapped.getTagKeys();
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
	public HolderOwner<T> asHolderOwner() {
		return this.wrapped.asHolderOwner();
	}

	@Override
	public RegistryLookup<T> asLookup() {
		return this.wrapped.asLookup();
	}

	@Override
	public Iterator<T> iterator() {
		return this.wrapped.iterator();
	}

	@Override
	public @Nullable T get(int index) {
		return this.wrapped.get(index);
	}

	@Override
	public RegistryKey<? extends Registry<T>> getKey() {
		return this.wrapped.getKey();
	}

	@Override
	public int size() {
		return this.wrapped.size();
	}

	@Override
	public Holder<T> set(int rawId, RegistryKey<T> key, T entry, Lifecycle lifecycle) {
		throw new UnsupportedOperationException("DelayedRegistry does not support set.");
	}

	@Override
	public Reference<T> register(RegistryKey<T> key, T entry, Lifecycle lifecycle) {
		this.delayedEntries.add(new DelayedEntry<>(key, entry, lifecycle));
		return Holder.Reference.create(this.wrapped.asHolderOwner(), key);
	}

	@Override
	public boolean empty() {
		return this.wrapped.empty();
	}

	@Override
	public HolderProvider<T> getHolderProvider() {
		return this.wrapped.getHolderProvider();
	}

	void applyDelayed() {
		DelayedEntry<T> entry;

		while ((entry = this.delayedEntries.poll()) != null) {
			this.wrapped.register(entry.key(), entry.entry(), entry.lifecycle());
		}
	}

	record DelayedEntry<T>(RegistryKey<T> key, T entry, Lifecycle lifecycle) {}
}
