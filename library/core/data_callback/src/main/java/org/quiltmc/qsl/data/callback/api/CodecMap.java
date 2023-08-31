/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.data.callback.api;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.event.Event;

/**
 * Relates codecs and identifiers, to be used for a dispatching codec without a full registry. {@link CodecAware} objects
 * parameterized by the type of this map can be encoded and decoded using the map's codec, and those that are encodable
 * should provide codec identifiers for a corresponding codec in this map.
 *
 * @param <T> the codec-aware type that this map dispatches to
 */
public class CodecMap<T extends CodecAware> {
	private final BiMap<Identifier, Codec<? extends T>> codecs = HashBiMap.create();

	public CodecMap() {}

	/**
	 * Creates a codec map pre-filled with an operation representing a value that has no effect. For instance, this can
	 * be used to initialize with an event callback that does nothing, or a biome modification that does not modify the
	 * biome. This provided value is useful as a value that datapacks can use to disable something.
	 *
	 * @param nullOperation a value that has no effect
	 */
	public CodecMap(T nullOperation) {
		this.codecs.put(new Identifier("quilt", "nothing"), Codec.unit(nullOperation));
	}

	/**
	 * Creates a delegating codec based off this map that captures both the encoded object and an event phase, storing
	 * the codec identifier in the "type" field and the phase in the "phase" field.
	 *
	 * @param descriptor a string describing the parameterized type of the codec, to be used in error messages
	 * @return a delegating codec based off this map
	 */
	public @NotNull Codec<Pair<Identifier, T>> createDelegatingCodecPhased(String descriptor) {
		return Codec.pair(Identifier.CODEC
				.optionalFieldOf("phase", Event.DEFAULT_PHASE).codec(), this.createDelegatingCodec(descriptor));
	}

	/**
	 * Creates a delegating codec based off this map that decodes or encodes objects, storing the codec identifier in
	 * the "type" field.
	 *
	 * @param descriptor A string describing the parameterized type of the codec, to be used in error messages.
	 * @return a delegating codec based off this map
	 */
	public @NotNull Codec<T> createDelegatingCodec(String descriptor) {
		return Identifier.CODEC.flatXmap(
				identifier ->
						this.lookup(identifier) == null
								? DataResult.<Codec<T>>error(() -> "Unregistered " + descriptor + " type: " + identifier)
								: DataResult.success(this.lookup(identifier)),
				codec -> {
					Identifier key = this.lookup(codec);

					if (key == null) {
						return DataResult.error(() -> "Unregistered " + descriptor + " type: " + codec);
					}

					return DataResult.success(key);
				}
		).partialDispatch("type", callback -> {
			var codecIdentifier = callback.getCodecId();
			var codec = codecIdentifier == null ? null : this.lookup(codecIdentifier);

			if (codec == null) {
				return DataResult.error(() -> "Codec not provided for " + descriptor + ": " + callback);
			}

			return DataResult.success(codec);
		}, DataResult::success);
	}

	/**
	 * Adds a codec to the map. Codecs based off this map will be able to delegate to the provided codec.
	 *
	 * @param id    the identifier to associate the codec with
	 * @param codec the codec to add
	 * @throws IllegalArgumentException if the identifier is already registered, or if the codec is already registered
	 */
	public void register(Identifier id, Codec<? extends T> codec) {
		if (this.codecs.containsKey(id)) {
			throw new IllegalArgumentException("Duplicate codec id: " + id);
		} else if (this.codecs.containsValue(codec)) {
			throw new IllegalArgumentException("Duplicate codec: " + codec);
		}

		this.codecs.put(id, codec);
	}

	/**
	 * {@return the codec associated with the provided identifier, or {@code null} if no codec is associated with the identifier}
	 *
	 * @param id the identifier to look up
	 */
	public Codec<? extends T> lookup(Identifier id) {
		return this.codecs.get(id);
	}

	/**
	 * {@return the identifier associated with the provided codec, or {@code null} if no identifier is associated with the codec}
	 *
	 * @param codec the codec to look up
	 */
	public Identifier lookup(Codec<? extends T> codec) {
		return this.codecs.inverse().get(codec);
	}
}
