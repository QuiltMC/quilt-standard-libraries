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
import net.minecraft.util.Identifier;
import java.util.HashMap;

public final class BuiltinRegistryEntryAttributeHolder<R> extends RegistryEntryAttributeHolderImpl<R> {
	private final HashMap<Identifier, RegistryEntryAttribute<R, ?>> attributes;

	public BuiltinRegistryEntryAttributeHolder() {
		attributes = new HashMap<>();
	}

	public <T> void registerAttribute(RegistryEntryAttribute<R, T> attribute) {
		attributes.put(attribute.getId(), attribute);
	}

	public RegistryEntryAttribute<R, ?> getAttribute(Identifier id) {
		return attributes.get(id);
	}
}
