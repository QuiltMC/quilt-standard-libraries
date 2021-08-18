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

import org.quiltmc.qsl.registry.attribute.api.RegistryExtensions;
import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import net.minecraft.util.registry.Registry;

public final class BuiltinRegistryEntryAttributeSetterImpl<R> implements RegistryExtensions.AttributeSetter<R> {
	private final R item;
	private final BuiltinRegistryEntryAttributeHolder<R> holder;

	public BuiltinRegistryEntryAttributeSetterImpl(Registry<R> registry, R item) {
		this.item = item;
		this.holder = RegistryEntryAttributeHolderImpl.getBuiltin(registry);
	}

	@Override
	public <T> RegistryExtensions.AttributeSetter<R> put(RegistryEntryAttribute<R, T> attrib, T value) {
		holder.putValue(item, attrib, value);
		return this;
	}
}
