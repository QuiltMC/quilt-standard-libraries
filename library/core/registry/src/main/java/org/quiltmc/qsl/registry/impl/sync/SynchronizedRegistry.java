/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.registry.impl.sync;

import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;

@ApiStatus.Internal
public interface SynchronizedRegistry<T> {
	void quilt$markForSync();

	boolean quilt$requiresSyncing();

	Status quilt$getContentStatus();

	Map<String, Collection<SyncEntry>> quilt$getSyncMap();

	void quilt$markDirty();

	void quilt$createIdSnapshot();

	void quilt$restoreIdSnapshot();

	Collection<MissingEntry> quilt$applySyncMap(Map<String, Collection<SyncEntry>> map);

	@SuppressWarnings("unchecked")
	static <T> SynchronizedRegistry<T> as(Registry<T> registry) {
		return (SynchronizedRegistry<T>) registry;
	}

	static void markForSync(Registry<?>... registries) {
		for (var reg : registries) {
			if (reg instanceof SynchronizedRegistry synchronizedRegistry) {
				synchronizedRegistry.quilt$markForSync();
			}
		}
	}

	void quilt$setRegistryFlag(RegistryFlag flag);

	byte quilt$getRegistryFlag();

	void quilt$setEntryFlag(T o, RegistryFlag flag);

	byte quilt$getEntryFlag(T o);

	record SyncEntry(String path, int rawId, byte flags) {}

	record MissingEntry(Identifier identifier, int rawId, byte flags) {}

	enum Status {
		VANILLA,
		OPTIONAL,
		REQUIRED;
	}
}
