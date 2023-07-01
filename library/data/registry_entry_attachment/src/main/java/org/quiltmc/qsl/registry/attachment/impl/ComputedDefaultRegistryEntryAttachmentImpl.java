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

package org.quiltmc.qsl.registry.attachment.impl;

import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.attachment.api.DefaultValueProvider;

public final class ComputedDefaultRegistryEntryAttachmentImpl<R, V> extends RegistryEntryAttachmentImpl<R, V> {
	private static final Logger COMPUTE_LOGGER = LoggerFactory.getLogger("RegistryEntryAttachment|Compute");

	private final @NotNull DefaultValueProvider<R, V> defaultValueProvider;

	public ComputedDefaultRegistryEntryAttachmentImpl(Registry<R> registry, Identifier id, Class<V> valueClass, Codec<V> codec, Side side, @NotNull DefaultValueProvider<R, V> defaultValueProvider) {
		super(registry, id, valueClass, codec, side);
		this.defaultValueProvider = defaultValueProvider;
	}

	@Override
	protected @Nullable V getDefaultValue(R entry) {
		var result = this.defaultValueProvider.computeDefaultValue(entry);

		if (result.hasFailed()) {
			COMPUTE_LOGGER.error("Failed to compute value for entry {}: {}", this.registry.getId(entry), result.error());
			return null;
		} else {
			var value = result.get();
			var encoded = codec.encodeStart(JsonOps.INSTANCE, value);

			if (encoded.result().isEmpty()) {
				if (encoded.error().isPresent()) {
					COMPUTE_LOGGER.error("Computed invalid value for entry {}: {}",
							this.registry.getId(entry), encoded.error().get().message());
				} else {
					COMPUTE_LOGGER.error("Computed invalid value for entry {}: unknown error",
							this.registry.getId(entry));
				}

				return null;
			}

			RegistryEntryAttachmentHolder.getBuiltin(this.registry).putValue(this, entry, value,
					BuiltinRegistryEntryAttachmentHolder.FLAG_COMPUTED);
			return value;
		}
	}
}
