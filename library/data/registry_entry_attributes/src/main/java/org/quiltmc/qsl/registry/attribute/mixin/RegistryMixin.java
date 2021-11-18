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
import org.quiltmc.qsl.registry.attribute.impl.QuiltRegistryInternals;
import org.quiltmc.qsl.registry.attribute.impl.RegistryEntryAttributeHolder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import java.util.HashMap;
import java.util.Map;

@Mixin(Registry.class)
public abstract class RegistryMixin<R> implements QuiltRegistryInternals<R> {
	@Unique private final Map<Identifier, RegistryEntryAttribute<R, ?>> attributes = new HashMap<>();
	@Unique private RegistryEntryAttributeHolder<R> quilt$builtinAttributeHolder;
	@Unique private RegistryEntryAttributeHolder<R> quilt$dataAttributeHolder;
	@Unique private RegistryEntryAttributeHolder<R> quilt$assetsAttributeHolder;

	@Override
	public void quilt$registerAttribute(RegistryEntryAttribute<R, ?> attribute) {
		attributes.put(attribute.id(), attribute);
	}

	@Override
	public @Nullable RegistryEntryAttribute<R, ?> quilt$getAttribute(Identifier id) {
		return attributes.get(id);
	}

	@Override
	public RegistryEntryAttributeHolder<R> quilt$getBuiltinAttributeHolder() {
		return quilt$builtinAttributeHolder;
	}

	@Override
	public void quilt$setBuiltinAttributeHolder(RegistryEntryAttributeHolder<R> holder) {
		this.quilt$builtinAttributeHolder = holder;
	}

	@Override
	public RegistryEntryAttributeHolder<R> quilt$getDataAttributeHolder() {
		return quilt$dataAttributeHolder;
	}

	@Override
	public void quilt$setDataAttributeHolder(RegistryEntryAttributeHolder<R> holder) {
		this.quilt$dataAttributeHolder = holder;
	}

	@Override
	public RegistryEntryAttributeHolder<R> quilt$getAssetsAttributeHolder() {
		return quilt$assetsAttributeHolder;
	}

	@Override
	public void quilt$setAssetsAttributeHolder(RegistryEntryAttributeHolder<R> holder) {
		this.quilt$assetsAttributeHolder = holder;
	}
}
