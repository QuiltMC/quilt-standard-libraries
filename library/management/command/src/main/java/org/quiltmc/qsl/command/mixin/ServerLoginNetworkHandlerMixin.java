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

package org.quiltmc.qsl.command.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.command.impl.KnownArgTypesStorage;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin implements KnownArgTypesStorage {
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

	@Inject(method = "addToServer", at = @At("HEAD"))
	public void passKnownArgumentTypesToPlayer(ServerPlayerEntity player, CallbackInfo ci) {
		((KnownArgTypesStorage) player).quilt$setKnownArgumentTypes(this.quilt$knownArgumentTypes);
	}
}
