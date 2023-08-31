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

package org.quiltmc.qsl.registry.mixin;

import java.util.IdentityHashMap;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.registry.Registry;
import net.minecraft.util.Util;

import org.quiltmc.qsl.registry.impl.sync.ProtocolVersions;
import org.quiltmc.qsl.registry.impl.sync.server.ExtendedConnectionClient;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin implements ExtendedConnectionClient {
	@Unique
	private IdentityHashMap<Registry<?>, ObjectOpenCustomHashSet<Object>> quilt$unknownEntries = new IdentityHashMap<>();
	@Unique
	private Object2IntMap<String> quilt$modProtocol = new Object2IntOpenHashMap<>();
	@Unique
	private boolean quilt$understandsOptional;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void quilt$setDefault(NetworkSide side, CallbackInfo ci) {
		this.quilt$modProtocol.defaultReturnValue(ProtocolVersions.NO_PROTOCOL);
	}

	@Override
	public void quilt$addUnknownEntry(Registry<?> registry, Object entry) {
		var set = this.quilt$unknownEntries.get(registry);

		if (set == null) {
			set = new ObjectOpenCustomHashSet<>(Util.identityHashStrategy());
			this.quilt$unknownEntries.put(registry, set);
		}

		set.add(entry);
	}

	@Override
	public boolean quilt$isUnknownEntry(Registry<?> registry, Object entry) {
		var set = this.quilt$unknownEntries.get(registry);

		return set != null && set.contains(entry);
	}

	@Override
	public boolean quilt$understandsOptional() {
		return this.quilt$understandsOptional;
	}

	@Override
	public void quilt$setUnderstandsOptional() {
		this.quilt$understandsOptional = true;
	}

	@Override
	public void quilt$setModProtocol(String id, int version) {
		this.quilt$modProtocol.put(id, version);
	}

	@Override
	public int quilt$getModProtocol(String id) {
		return this.quilt$modProtocol.getInt(id);
	}
}
