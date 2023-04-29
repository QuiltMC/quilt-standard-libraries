/*
 * Copyright 2023 QuiltMC
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

import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.network.ClientConnection;
import net.minecraft.registry.Registry;
import net.minecraft.util.Util;
import org.quiltmc.qsl.registry.impl.sync.server.SyncAwareConnectionClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.IdentityHashMap;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin implements SyncAwareConnectionClient {
	@Unique
	private IdentityHashMap<Registry<?>, ObjectOpenCustomHashSet<Object>> quilt$unknownEntries = new IdentityHashMap<>();
	@Unique
	private boolean quilt$understandsOptional;

	@Override
	public void quilt$addUnknownEntry(Registry<?> registry, Object entry) {
		var set = quilt$unknownEntries.get(registry);

		if (set == null) {
			set = new ObjectOpenCustomHashSet<>(Util.identityHashStrategy());
			quilt$unknownEntries.put(registry, set);
		}

		set.add(entry);
	}

	@Override
	public boolean quilt$isUnknownEntry(Registry<?> registry, Object entry) {
		var set = quilt$unknownEntries.get(registry);

		return set != null && set.contains(entry);
	}

	@Override
	public void quilt$clearUnknown() {
		this.quilt$unknownEntries.clear();
	}

	@Override
	public boolean quilt$understandsOptional() {
		return this.quilt$understandsOptional;
	}

	@Override
	public void quilt$setUnderstandsOptional() {
		this.quilt$understandsOptional = true;
	}
}
