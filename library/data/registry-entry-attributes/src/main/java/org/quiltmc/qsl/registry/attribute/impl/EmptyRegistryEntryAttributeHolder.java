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

public final class EmptyRegistryEntryAttributeHolder<R> implements RegistryEntryAttributeHolder<R> {
	@SuppressWarnings("unchecked")
	public static <R> RegistryEntryAttributeHolder<R> get() {
		return (RegistryEntryAttributeHolder<R>) INSTANCE;
	}

	private static final RegistryEntryAttributeHolder<?> INSTANCE = new EmptyRegistryEntryAttributeHolder<>();

	private EmptyRegistryEntryAttributeHolder() { }

	@Override
	public <T> Optional<T> getValue(R item, RegistryEntryAttribute<R, T> attribute) {
		return Optional.empty();
	}
}
