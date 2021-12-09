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

package org.quiltmc.qsl.registry.dict.impl;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.dict.api.RegistryDict;

@ApiStatus.Internal
public interface QuiltRegistryInternals<R> {
	void quilt$registerDict(RegistryDict<R, ?> dictionary);

	@Nullable RegistryDict<R, ?> quilt$getDict(Identifier id);

	RegistryDictHolder<R> quilt$getBuiltinDictHolder();

	void quilt$setBuiltinDictHolder(RegistryDictHolder<R> holder);

	RegistryDictHolder<R> quilt$getDataDictHolder();

	void quilt$setDataDictHolder(RegistryDictHolder<R> holder);

	RegistryDictHolder<R> quilt$getAssetsDictHolder();

	void quilt$setAssetsDictHolder(RegistryDictHolder<R> holder);
}
