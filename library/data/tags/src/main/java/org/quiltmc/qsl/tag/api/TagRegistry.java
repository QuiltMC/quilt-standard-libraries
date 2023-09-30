/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.tag.api;

import java.util.Collection;
import java.util.stream.Stream;

import net.minecraft.registry.Holder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;

import org.quiltmc.qsl.tag.impl.TagRegistryImpl;
import org.quiltmc.qsl.tag.impl.client.ClientTagRegistryManager;

/**
 * Represents a tag registry.
 */
public final class TagRegistry {
	private TagRegistry() {
		throw new UnsupportedOperationException("TagRegistry only contains static definitions.");
	}

	/**
	 * {@return a stream of the normal populated tags of the given registry}
	 *
	 * @param registry the registry of the values of the tag
	 * @param <T>      the type of the values held by the tag
	 */
	public static <T> Stream<TagValues<T>> stream(RegistryKey<? extends Registry<T>> registry) {
		return stream(registry, TagType.NORMAL);
	}

	/**
	 * {@return a stream of the populated tags of the given registry}
	 *
	 * @param registry the registry of the values of the tag
	 * @param type     the type of tags, {@link TagType#CLIENT_FALLBACK} will offer normal tags or the fallback if not present
	 * @param <T>      the type of the values held by the tag
	 */
	public static <T> Stream<TagValues<T>> stream(RegistryKey<? extends Registry<T>> registry, TagType type) {
		return switch (type) {
			case NORMAL -> TagRegistryImpl.streamTags(registry);
			case CLIENT_FALLBACK -> TagRegistryImpl.streamTagsWithFallback(registry);
			case CLIENT_ONLY -> ClientTagRegistryManager.get(registry).streamClientTags();
		};
	}

	/**
	 * Returns the currently populated tag of the corresponding tag key.
	 *
	 * @param key the key
	 * @param <T> the type of the values held by the tag
	 * @return the populated tag, always empty if the tag doesn't exist or isn't populated yet
	 */
	public static <T> Collection<Holder<T>> getTag(TagKey<T> key) {
		return TagRegistryImpl.getTag(key);
	}

	/**
	 * Represents a tag entry for iteration.
	 *
	 * @param <T> the type of the values held by the tag
	 */
	public record TagValues<T>(TagKey<T> key, Collection<Holder<T>> values) {
	}
}
