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
import org.quiltmc.qsl.registry.attribute.impl.RegistryEntryAttributeHolder;
import org.quiltmc.qsl.registry.attribute.impl.CombinedRegistryEntryAttributeHolder;
import org.quiltmc.qsl.registry.attribute.impl.QuiltRegistryInternals;
import org.quiltmc.qsl.registry.attribute.impl.RegistryEntryAttributeHolderImpl;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import java.util.HashMap;

@Mixin(Registry.class)
public abstract class RegistryMixin<R> implements QuiltRegistryInternals<R> {
	@Unique private final HashMap<Identifier, RegistryEntryAttribute<R, ?>> attributes = new HashMap<>();
	@Unique private RegistryEntryAttributeHolderImpl<R> qsl$builtinAttributeHolder;
	@Unique private RegistryEntryAttributeHolderImpl<R> qsl$dataAttributeHolder;
	@Unique private RegistryEntryAttributeHolder<R> qsl$combinedAttributeHolder;

	@Override
	public void qsl$registerAttribute(RegistryEntryAttribute<R, ?> attribute) {
		attributes.put(attribute.getId(), attribute);
	}

	@Override
	public @Nullable RegistryEntryAttribute<R, ?> qsl$getAttribute(Identifier id) {
		return attributes.get(id);
	}

	@Override
	public RegistryEntryAttributeHolderImpl<R> qsl$getBuiltinAttributeHolder() {
		return qsl$builtinAttributeHolder;
	}

	@Override
	public void qsl$setBuiltinAttributeHolder(RegistryEntryAttributeHolderImpl<R> holder) {
		this.qsl$builtinAttributeHolder = holder;
		qsl$updateCombinedAttributeHolder();
	}

	@Override
	public RegistryEntryAttributeHolderImpl<R> qsl$getDataAttributeHolder() {
		return qsl$dataAttributeHolder;
	}

	@Override
	public void qsl$setDataAttributeHolder(RegistryEntryAttributeHolderImpl<R> holder) {
		this.qsl$dataAttributeHolder = holder;
		qsl$updateCombinedAttributeHolder();
	}

	@Override
	public RegistryEntryAttributeHolder<R> qsl$getCombinedAttributeHolder() {
		if (qsl$combinedAttributeHolder == null) {
			qsl$updateCombinedAttributeHolder();
		}
		return qsl$combinedAttributeHolder;
	}

	@Unique private void qsl$updateCombinedAttributeHolder() {
		qsl$combinedAttributeHolder = new CombinedRegistryEntryAttributeHolder<>(
				qsl$dataAttributeHolder, qsl$builtinAttributeHolder);
	}
}
