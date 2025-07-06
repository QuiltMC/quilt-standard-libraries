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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.feature_flags.FeatureFlagBitSet;
import net.minecraft.registry.Holder;
import net.minecraft.registry.HolderOwner;
import net.minecraft.registry.HolderProvider;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.RegistrationInfo;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.registry.Holder.Reference;
import net.minecraft.registry.HolderSet.NamedSet;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.random.RandomGenerator;

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
	public Optional<RegistrationInfo> getRegistrationInfo(RegistryKey<T> registryKey) {
		return this.wrapped.getRegistrationInfo(registryKey);
	}

	@Override
	public Optional<T> getOrEmpty(@Nullable Identifier id) {
		return this.wrapped.getOrEmpty(id);
	}

	@Override
	public Optional<T> getOrEmpty(@Nullable RegistryKey<T> key) {
		return this.wrapped.getOrEmpty(key);
	}

	@Override
	public Optional<Reference<T>> findAny() {
		return this.wrapped.findAny();
	}

	@Override
	public T getOrThrow(RegistryKey<T> key) {
		return this.wrapped.getOrThrow(key);
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
	public Stream<T> stream() {
		return this.wrapped.stream();
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
	public Optional<Reference<T>> find(int i) {
		return this.wrapped.find(i);
	}

	@Override
	public Optional<Reference<T>> find(Identifier id) {
		return this.wrapped.find(id);
	}

	@Override
	public Optional<Reference<T>> getHolder(RegistryKey<T> key) {
		return this.wrapped.getHolder(key);
	}

	@Override
	public Reference<T> getHolderOrThrow(RegistryKey<T> key) {
		return this.wrapped.getHolderOrThrow(key);
	}

	@Override
	public Holder<T> wrapAsHolder(T object) {
		return this.wrapped.wrapAsHolder(object);
	}

	@Override
	public Iterable<Holder<T>> getTagOrEmpty(TagKey<T> tag) {
		return this.wrapped.getTagOrEmpty(tag);
	}

	@Override
	public Optional<Holder<T>> getRandomElement(TagKey<T> tag, RandomGenerator random) {
		return this.wrapped.getRandomElement(tag, random);
	}

	@Override
	public Stream<NamedSet<T>> streamBoundTags() {
		return this.wrapped.streamBoundTags();
	}

	@Override
	public IndexedIterable<Holder<T>> asHolderIdMap() {
		return this.wrapped.asHolderIdMap();
	}

	@Override
	public PendingTags<T> startTagReload(TagGroupLoader.RegistryTags<T> registryTags) {
		return this.wrapped.startTagReload(registryTags);
	}

	@Override
	public Optional<NamedSet<T>> getTag(TagKey<T> tag) {
		return this.wrapped.getTag(tag);
	}

	@Override
	public NamedSet<T> getTagOrThrow(TagKey<T> tagKey) {
		return this.wrapped.getTagOrThrow(tagKey);
	}

	@Override
	@NotNull public Iterator<T> iterator() {
		return this.wrapped.iterator();
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		this.wrapped.forEach(action);
	}

	@Override
	public Spliterator<T> spliterator() {
		return this.wrapped.spliterator();
	}

	@Override
	public @Nullable T get(int index) {
		return this.wrapped.get(index);
	}

	@Override
	public T getOrThrow(int index) {
		return this.wrapped.getOrThrow(index);
	}

	@Override
	public int getRawIdOrThrow(T value) {
		return this.wrapped.getRawIdOrThrow(value);
	}

	@Override
	public RegistryKey<? extends Registry<T>> getKey() {
		return this.wrapped.getKey();
	}

	@Override
	public Codec<T> getCodec() {
		return this.wrapped.getCodec();
	}

	@Override
	public Codec<Holder<T>> holderByNameCodec() {
		return this.wrapped.holderByNameCodec();
	}

	@Override
	public <U> Stream<U> keys(DynamicOps<U> dynamicOps) {
		return this.wrapped.keys(dynamicOps);
	}

	@Override
	public Lifecycle getRegistryLifecycle() {
		return this.wrapped.getRegistryLifecycle();
	}

	@Override
	public RegistryLookup<T> enabledIn(FeatureFlagBitSet featureFlags) {
		return this.wrapped.enabledIn(featureFlags);
	}

	@Override
	public RegistryLookup<T> withFilter(Predicate<T> predicate) {
		return this.wrapped.withFilter(predicate);
	}

	@Override
	public int size() {
		return this.wrapped.size();
	}

	@Override
	public Reference<T> register(RegistryKey<T> registryKey, T object, RegistrationInfo registrationInfo) {
		return this.wrapped.register(registryKey, object, registrationInfo);
	}

	@Override
	public void bindTag(TagKey<T> tag, List<Holder<T>> list) {
		this.wrapped.bindTag(tag, list);
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
			this.wrapped.register(entry.key(), entry.entry(), entry.info());
		}
	}

	@Override
	public Stream<Reference<T>> streamHolders() {
		return this.wrapped.streamHolders();
	}

	@Override
	public Stream<RegistryKey<T>> streamElementKeys() {
		return this.wrapped.streamElementKeys();
	}

	@Override
	public Stream<NamedSet<T>> streamTags() {
		return this.wrapped.streamTags();
	}

	@Override
	public Stream<TagKey<T>> streamTagKeys() {
		return this.wrapped.streamTagKeys();
	}

	@Override
	public boolean isSame(HolderOwner<T> owner) {
		return this.wrapped.isSame(owner);
	}

	record DelayedEntry<T>(RegistryKey<T> key, T entry, RegistrationInfo info) { }
}
