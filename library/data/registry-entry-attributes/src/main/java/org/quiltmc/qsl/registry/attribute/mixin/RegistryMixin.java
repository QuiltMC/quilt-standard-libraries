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

package org.quiltmc.qsl.registry.attribute.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttributeHolder;
import org.quiltmc.qsl.registry.attribute.impl.CombinedRegistryEntryAttributeHolder;
import org.quiltmc.qsl.registry.attribute.impl.QuiltRegistryInternals;
import org.quiltmc.qsl.registry.attribute.impl.RegistryEntryAttributeHolderImpl;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import java.util.HashMap;

@Mixin(Registry.class)
public abstract class RegistryMixin implements QuiltRegistryInternals {
	@Unique private final HashMap<Identifier, RegistryEntryAttribute<?, ?>> attributes = new HashMap<>();
	@Unique private RegistryEntryAttributeHolderImpl<?> qsl$builtinAttributeHolder;
	@Unique private RegistryEntryAttributeHolderImpl<?> qsl$dataAttributeHolder;
	@Unique private RegistryEntryAttributeHolder<?> qsl$combinedAttributeHolder;

	@Override
	public void qsl$registerAttribute(RegistryEntryAttribute<?, ?> attribute) {
		attributes.put(attribute.getId(), attribute);
	}

	@Override
	public @Nullable RegistryEntryAttribute<?, ?> qsl$getAttribute(Identifier id) {
		return attributes.get(id);
	}

	@Override
	public RegistryEntryAttributeHolderImpl<?> qsl$getBuiltinAttributeHolder() {
		return qsl$builtinAttributeHolder;
	}

	@Override
	public void qsl$setBuiltinAttributeHolder(RegistryEntryAttributeHolderImpl<?> holder) {
		this.qsl$builtinAttributeHolder = holder;
		qsl$updateCombinedAttributeHolder();
	}

	@Override
	public RegistryEntryAttributeHolderImpl<?> qsl$getDataAttributeHolder() {
		return qsl$dataAttributeHolder;
	}

	@Override
	public void qsl$setDataAttributeHolder(RegistryEntryAttributeHolderImpl<?> holder) {
		this.qsl$dataAttributeHolder = holder;
		qsl$updateCombinedAttributeHolder();
	}

	@Override
	public RegistryEntryAttributeHolder<?> qsl$getCombinedAttributeHolder() {
		return qsl$combinedAttributeHolder;
	}

	@SuppressWarnings("unchecked")
	@Unique private void qsl$updateCombinedAttributeHolder() {
		qsl$combinedAttributeHolder = new CombinedRegistryEntryAttributeHolder<>(
				(RegistryEntryAttributeHolder<Object>) qsl$dataAttributeHolder,
				(RegistryEntryAttributeHolder<Object>) qsl$builtinAttributeHolder);
	}
}
