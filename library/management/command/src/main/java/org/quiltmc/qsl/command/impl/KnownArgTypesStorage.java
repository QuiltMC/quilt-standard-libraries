/*
 * Copyright 2016, 2017, 2018, 2019, 2020 zml and Colonel contributors
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.command.impl;

import java.util.Set;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.Identifier;

@ApiStatus.Internal
public interface KnownArgTypesStorage {
	Set<Identifier> quilt$getKnownArgumentTypes();

	void quilt$setKnownArgumentTypes(Set<Identifier> types);
}
