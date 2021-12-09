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

package org.quiltmc.qsl.registry.dict.mixin;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.registry.dict.api.RegistryDict;
import org.quiltmc.qsl.registry.dict.impl.QuiltRegistryInternals;
import org.quiltmc.qsl.registry.dict.impl.RegistryDictHolder;

@Mixin(Registry.class)
public abstract class RegistryMixin<R> implements QuiltRegistryInternals<R> {
	@Unique
	private final Map<Identifier, RegistryDict<R, ?>> quilt$dicts = new HashMap<>();
	@Unique
	private RegistryDictHolder<R> quilt$builtinDictHolder;
	@Unique
	private RegistryDictHolder<R> quilt$dataDictHolder;
	@Unique
	private RegistryDictHolder<R> quilt$assetsDictHolder;

	@Override
	public void quilt$registerDict(RegistryDict<R, ?> attribute) {
		quilt$dicts.put(attribute.id(), attribute);
	}

	@Override
	public @Nullable RegistryDict<R, ?> quilt$getDict(Identifier id) {
		return quilt$dicts.get(id);
	}

	@Override
	public RegistryDictHolder<R> quilt$getBuiltinDictHolder() {
		return quilt$builtinDictHolder;
	}

	@Override
	public void quilt$setBuiltinDictHolder(RegistryDictHolder<R> holder) {
		this.quilt$builtinDictHolder = holder;
	}

	@Override
	public RegistryDictHolder<R> quilt$getDataDictHolder() {
		return quilt$dataDictHolder;
	}

	@Override
	public void quilt$setDataDictHolder(RegistryDictHolder<R> holder) {
		this.quilt$dataDictHolder = holder;
	}

	@Override
	public RegistryDictHolder<R> quilt$getAssetsDictHolder() {
		return quilt$assetsDictHolder;
	}

	@Override
	public void quilt$setAssetsDictHolder(RegistryDictHolder<R> holder) {
		this.quilt$assetsDictHolder = holder;
	}
}
