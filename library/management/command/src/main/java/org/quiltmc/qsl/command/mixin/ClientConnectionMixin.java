/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.command.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.network.ClientConnection;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.command.impl.KnownArgTypesStorage;

@Mixin(ClientConnection.class)
public abstract class ClientConnectionMixin implements KnownArgTypesStorage {
	@Unique
	private Set<Identifier> quilt$knownArgumentTypes;

	@Override
	public Set<Identifier> quilt$getKnownArgumentTypes() {
		return this.quilt$knownArgumentTypes;
	}

	@Override
	public void quilt$setKnownArgumentTypes(Set<Identifier> types) {
		this.quilt$knownArgumentTypes = types;
	}
}
