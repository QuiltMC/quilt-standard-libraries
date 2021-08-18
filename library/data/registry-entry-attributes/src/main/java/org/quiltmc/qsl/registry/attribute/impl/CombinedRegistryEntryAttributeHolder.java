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
	private final RegistryEntryAttributeHolder<R> delegate, fallback;

	public CombinedRegistryEntryAttributeHolder(RegistryEntryAttributeHolder<R> delegate, RegistryEntryAttributeHolder<R> fallback) {
		this.delegate = delegate;
		this.fallback = fallback;
	}

	@Override
	public <T> Optional<T> getValue(R item, RegistryEntryAttribute<R, T> attribute) {
		Optional<T> opt = Optional.empty();
		if (delegate != null) {
			opt = delegate.getValue(item, attribute);
			if (opt.isPresent())
				return opt;
		}
		if (fallback != null) {
			opt = fallback.getValue(item, attribute);
			if (opt.isPresent())
				return opt;
		}
		return opt;
	}
}
