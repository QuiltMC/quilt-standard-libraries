/*
 * Copyright 2022 The Quilt Project
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

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.util.InjectedInterface;
import org.quiltmc.qsl.tag.impl.QuiltTagKeyHooks;
import org.quiltmc.qsl.tag.mixin.TagKeyAccessor;

/**
 * Interface implemented by {@link net.minecraft.registry.tag.TagKey} instances when QSL is present.
 *
 * @param <T> the type of the values held by the tag
 * @see #of(RegistryKey, Identifier, TagType)
 */
@InjectedInterface(TagKey.class)
public interface QuiltTagKey<T> {
	/**
	 * {@return the type of tag of this key}
	 */
	TagType type();

	/**
	 * Creates a new tag key based on another key but with a different type.
	 *
	 * @param key  the tag key to base the new one on
	 * @param type the type of the tag
	 * @return the new tag key
	 */
	static <T> TagKey<T> of(TagKey<T> key, TagType type) {
		return QuiltTagKey.of(key.registry(), key.id(), type);
	}

	/**
	 * Creates a new tag key.
	 *
	 * @param registry the registry for which this tag key is valid
	 * @param id       the identifier of the tag
	 * @param type     the type of the tag
	 * @param <T>      the type of the values held by the tag
	 * @return the tag key
	 */
	@SuppressWarnings({"deprecation", "unchecked"})
	static <T> TagKey<T> of(RegistryKey<? extends Registry<T>> registry, Identifier id, TagType type) {
		var key = new TagKey<>(registry, id);
		((QuiltTagKeyHooks) (Object) key).quilt$setType(type);
		return (TagKey<T>) TagKeyAccessor.getInterner().intern(key);
	}
}
