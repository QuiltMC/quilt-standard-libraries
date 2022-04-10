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

package org.quiltmc.qsl.registry.mixin;

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.util.Holder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.registry.api.sync.RegistryFlag;
import org.quiltmc.qsl.registry.impl.event.MutableRegistryEntryContextImpl;
import org.quiltmc.qsl.registry.impl.event.RegistryEventStorage;
import org.quiltmc.qsl.registry.impl.sync.SynchronizedRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

/**
 * Stores and invokes registry events.
 */
@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<V> extends Registry<V> implements SynchronizedRegistry<V> {
	@Unique
	private final MutableRegistryEntryContextImpl<V> quilt$entryContext = new MutableRegistryEntryContextImpl<>(this);

	@Shadow
	@Final
	private Object2IntMap<V> entryToRawId;

	@Shadow
	@Final
	private ObjectList<Holder.Reference<V>> rawIdToEntry;

	@Shadow
	@Final
	private Map<Identifier, Holder.Reference<V>> byId;

	@Shadow
	private @Nullable List<Holder.Reference<V>> holdersInOrder;

	@Unique
	private boolean quilt$shouldSync = false;

	@Unique
	@Nullable
	private Map<String, Collection<SyncEntry>> quilt$syncMap;

	@Unique
	@Nullable
	private ObjectList<Holder.Reference<V>> quilt$idSnapshot;

	@Unique
	private byte quilt$flags;

	@Unique
	private Object2ByteMap<V> quilt$entryToFlag = new Object2ByteOpenHashMap<>();

	protected SimpleRegistryMixin(RegistryKey<? extends Registry<V>> key, Lifecycle lifecycle) {
		super(key, lifecycle);
	}

	/**
	 * Invokes the entry add event.
	 */
	@Inject(
			method = "registerMapping",
			at = @At("RETURN")
	)
	private void quilt$invokeEntryAddEvent(int rawId, RegistryKey<V> key, V entry, Lifecycle lifecycle, boolean checkDuplicateKeys,
	                                       CallbackInfoReturnable<Holder<V>> cir) {
		this.quilt$entryContext.set(key.getValue(), entry, rawId);
		RegistryEventStorage.as(this).quilt$getEntryAddedEvent().invoker().onAdded(this.quilt$entryContext);

		this.quilt$syncMap = null;
	}

	@Override
	public void quilt$markForSync() {
		this.quilt$shouldSync = true;
	}

	@Override
	public boolean quilt$requiresSyncing() {
		return this.quilt$shouldSync;
	}

	@Override
	public Map<String, Collection<SyncEntry>> quilt$getSyncMap() {
		if (this.quilt$syncMap == null) {
			var map = new HashMap<String, Collection<SyncEntry>>();

			this.entryToRawId.forEach((entry, key) -> {
				var identifier = this.getId(entry);
				var flag = this.quilt$entryToFlag.getOrDefault(entry, (byte) 0);


				if (!RegistryFlag.isSkipped(flag)) {
					map.computeIfAbsent(
							identifier.getNamespace(),
							(n) -> new ArrayList<>()
					).add(new SyncEntry(identifier.getPath(), key, this.quilt$entryToFlag.getOrDefault(entry, flag)));
				}
			});

			this.quilt$syncMap = map;
		}

		return this.quilt$syncMap;
	}

	@Override
	public Collection<MissingEntry> quilt$applySyncMap(Map<String, Collection<SyncEntry>> entries) {
		if (this.quilt$idSnapshot == null) {
			this.quilt$createIdSnapshot();
		}
		var list = new ArrayList<MissingEntry>();

		for (var key : entries.keySet()) {

			for (var idEntry : entries.get(key)) {
				var identifier = new Identifier(key, idEntry.path());
				var holder = this.byId.get(identifier);

				if (holder != null) {
					this.entryToRawId.put(holder.value(), idEntry.rawId());

					while (this.rawIdToEntry.size() <= idEntry.rawId()) {
						this.rawIdToEntry.add(null);
					}
					this.rawIdToEntry.set(idEntry.rawId(), holder);
				} else {
					list.add(new MissingEntry(identifier, idEntry.rawId(), idEntry.flags()));
				}
			}
		}

		this.holdersInOrder = null;

		return list;
	}

	@Override
	public void quilt$markDirty() {
		this.quilt$syncMap = null;
	}

	@Override
	public void quilt$setRegistryFlag(RegistryFlag flag) {
		this.quilt$flags = (byte) (this.quilt$flags | flag.ordinal());
	}

	@Override
	public byte quilt$getRegistryFlag() {
		return this.quilt$flags;
	}

	@Override
	public void quilt$setEntryFlag(V o, RegistryFlag flag) {
		this.quilt$entryToFlag.put(o, (byte) (this.quilt$entryToFlag.getByte(o) | flag.ordinal()));
	}

	@Override
	public void quilt$createIdSnapshot() {
		if (this.quilt$idSnapshot != null) {
			throw new RuntimeException("Registry snapshot already exists!");
		}
		this.quilt$idSnapshot = new ObjectArrayList<>(this.rawIdToEntry);
	}

	@Override
	public void quilt$restoreIdSnapshot() {
		if (this.quilt$idSnapshot != null) {
			this.rawIdToEntry.clear();
			this.rawIdToEntry.addAll(this.quilt$idSnapshot);
			var size = this.rawIdToEntry.size();

			for (int i = 0; i < size; i++) {
				var entry = this.rawIdToEntry.get(i);
				if (entry != null) {
					this.entryToRawId.put(entry.value(), i);
				}
			}

			this.quilt$idSnapshot = null;
		}
	}
}
