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

package org.quiltmc.qsl.registry.attribute.impl;

import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttributeHolder;
import java.util.Optional;

@SuppressWarnings("ClassCanBeRecord")
public final class CombinedRegistryEntryAttributeHolder<R> implements RegistryEntryAttributeHolder<R> {
	private final RegistryEntryAttributeHolderImpl<R> delegate, fallback;

	public CombinedRegistryEntryAttributeHolder(RegistryEntryAttributeHolderImpl<R> delegate, RegistryEntryAttributeHolderImpl<R> fallback) {
		this.delegate = delegate;
		this.fallback = fallback;
	}

	@Override
	public <T> Optional<T> getValue(R entry, RegistryEntryAttribute<R, T> attribute) {
		T value;
		if (delegate != null) {
			value = delegate.getValueNoDefault(entry, attribute);
			if (value != null) {
				return Optional.of(value);
			}
		}
		if (fallback != null) {
			value = fallback.getValueNoDefault(entry, attribute);
			if (value != null) {
				return Optional.of(value);
			}
		}
		return Optional.ofNullable(attribute.getDefaultValue());
	}
}
