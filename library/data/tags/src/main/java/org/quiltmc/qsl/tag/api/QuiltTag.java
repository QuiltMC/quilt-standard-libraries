/*
 * Copyright 2021 QuiltMC
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

import net.minecraft.tag.Tag;

/**
 * Interface implemented by {@link net.minecraft.tag.Tag} instances when QSL is present.
 * <p>
 * The usage of normal Java cast is discouraged, please use {@link #getExtensions(Tag)} instead as it will ensure the correct
 * generic types.
 * Implementations of {@link net.minecraft.tag.Tag} may implement this interface to retain full functionality,
 * a default implementation is provided to ensure compatibility.
 *
 * @param <T> the type of the values held by the tag
 */
public interface QuiltTag<T> {
	/**
	 * Gets the type of this tag. The type will define how this tag is handled for syncing, requirement, etc.
	 *
	 * @return the type of this tag
	 */
	TagType getType();

	/**
	 * {@return {@code true} if the given tag has been "replaced" by a data pack at least once}
	 * <p>
	 * The use case for such method is to be able to provide vanilla behaviour compatibility in a hook,
	 * but also allow overriding it for "total conversion" data-packs.
	 */
	boolean hasBeenReplaced();

	/**
	 * {@return the corresponding Quilt tag}
	 *
	 * @param tag the tag to cast
	 * @param <T> the type of the values held by the tag
	 */
	static <T> QuiltTag<T> getExtensions(Tag<T> tag) {
		//noinspection unchecked
		return (QuiltTag<T>) tag;
	}
}
