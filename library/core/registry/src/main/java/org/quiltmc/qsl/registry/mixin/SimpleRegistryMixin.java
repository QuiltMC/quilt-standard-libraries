/*
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

package org.quiltmc.qsl.registry.mixin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.Holder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;
import org.quiltmc.qsl.registry.impl.event.MutableRegistryEntryContextImpl;
import org.quiltmc.qsl.registry.impl.event.RegistryEventStorage;
import org.quiltmc.qsl.registry.impl.sync.registry.RegistryFlag;
import org.quiltmc.qsl.registry.impl.sync.registry.SynchronizedRegistry;
import org.quiltmc.qsl.registry.impl.sync.server.ServerRegistrySync;

/**
 * Stores and invokes registry events.
 * Handles applying and creating sync data.
 */
@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<V> implements Registry<V>, SynchronizedRegistry<V>, RegistryEventStorage<V> {
	@Unique
	private MutableRegistryEntryContextImpl<V> quilt$entryContext;

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
	@Nullable
	private List<Holder.Reference<V>> holdersInOrder;

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
	@Nullable
	private Status quilt$syncStatus = null;

	@Unique
	private Object2ByteMap<V> quilt$entryToFlag;

	@Unique
	private Event<RegistryEvents.EntryAdded<V>> quilt$entryAddedEvent;

	// HACK TODO for some reason initializing this like normal doesnt work. i dont care to figure out why - glitch
	@Inject(method = "<init>(Lnet/minecraft/registry/RegistryKey;Lcom/mojang/serialization/Lifecycle;Z)V", at = @At("TAIL"))
	private void hackBecauseMixinHatesMe(RegistryKey key, Lifecycle lifecycle, boolean useIntrusiveHolders, CallbackInfo ci) {
		this.quilt$entryContext = new MutableRegistryEntryContextImpl<>(this);
		this.quilt$entryToFlag = new Object2ByteOpenHashMap<>();
		this.quilt$entryAddedEvent = Event.create(RegistryEvents.EntryAdded.class,
			callbacks -> context -> {
				for (var callback : callbacks) {
					callback.onAdded(context);
				}
			});
	}

	@SuppressWarnings("InvalidInjectorMethodSignature")
	@ModifyVariable(
			method = "set(ILnet/minecraft/registry/RegistryKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/registry/Holder$Reference;",
			slice = @Slice(
					from = @At(
							value = "INVOKE",
							target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;",
							remap = false
					)
			),
			at = @At(
					value = "STORE",
					ordinal = 0
			)
	)
	private Holder.Reference<V> quilt$eagerFillReference(Holder.Reference<V> reference, int rawId, RegistryKey<V> key, V entry, Lifecycle lifecycle) {
		reference.setValue(entry);
		return reference;
	}

	/**
	 * Invokes the entry add event.
	 */
	@SuppressWarnings({"ConstantConditions", "unchecked"})
	@Inject(
			method = "set(ILnet/minecraft/registry/RegistryKey;Ljava/lang/Object;Lcom/mojang/serialization/Lifecycle;)Lnet/minecraft/registry/Holder$Reference;",
			at = @At("RETURN")
	)
	private void quilt$invokeEntryAddEvent(int rawId, RegistryKey<V> key, V entry, Lifecycle lifecycle, CallbackInfoReturnable<Holder<V>> cir) {
		this.quilt$entryContext.set(key.getValue(), entry, rawId);
		RegistryEventStorage.as((SimpleRegistry<V>) (Object) this).quilt$getEntryAddedEvent().invoker().onAdded(this.quilt$entryContext);

		this.quilt$markDirty();
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
	public Status quilt$getContentStatus() {
		if (this.quilt$syncStatus == null) {
			var status = Status.VANILLA;
			var optional = RegistryFlag.isOptional(this.quilt$flags);
			for (var entry : this.rawIdToEntry) {
				if (entry == null) continue;

				var namespace = entry.getRegistryKey().getValue().getNamespace();
				if (!ServerRegistrySync.isNamespaceVanilla(namespace)) {
					var flag = this.quilt$entryToFlag.getOrDefault(entry.value(), (byte) 0);
					if (!RegistryFlag.isSkipped(flag)) {
						if (RegistryFlag.isOptional(flag)) {
							status = Status.OPTIONAL;
							if (optional) {
								break;
							}
						} else {
							status = optional ? Status.OPTIONAL : Status.REQUIRED;
							break;
						}
					}
				}
			}

			this.quilt$syncStatus = status;
		}

		return this.quilt$syncStatus;
	}

	@Override
	public Collection<MissingEntry> quilt$applySyncMap(Map<String, Collection<SyncEntry>> entries) {
		if (this.quilt$idSnapshot == null) {
			this.quilt$createIdSnapshot();
		}

		var missingEntries = new ArrayList<MissingEntry>();

		var holders = new ArrayList<>(this.rawIdToEntry);
		int currentId = 0;

		this.entryToRawId.clear();
		this.rawIdToEntry.clear();

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

					holders.remove(holder);
					currentId = Math.max(currentId, idEntry.rawId());
				} else {
					missingEntries.add(new MissingEntry(identifier, idEntry.rawId(), idEntry.flags()));
				}
			}
		}

		while (this.rawIdToEntry.size() <= currentId + holders.size()) {
			this.rawIdToEntry.add(null);
		}

		for (var holder : holders) {
			if (holder == null) continue;

			var id = ++currentId;
			this.entryToRawId.put(holder.value(), id);
			this.rawIdToEntry.set(id, holder);
		}

		this.holdersInOrder = null;

		return missingEntries;
	}

	@Override
	public void quilt$markDirty() {
		this.quilt$syncMap = null;
		this.quilt$syncStatus = null;
	}

	@Override
	public void quilt$setRegistryFlag(RegistryFlag flag) {
		this.quilt$flags = (byte) (this.quilt$flags | (0x1 << flag.ordinal()));
	}

	@Override
	public byte quilt$getRegistryFlag() {
		return this.quilt$flags;
	}

	@Override
	public void quilt$setEntryFlag(V o, RegistryFlag flag) {
		this.quilt$entryToFlag.put(o, (byte) (this.quilt$entryToFlag.getByte(o) | (0x1 << flag.ordinal())));
	}

	@Override
	public byte quilt$getEntryFlag(V o) {
		return this.quilt$entryToFlag.getByte(o);
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

	@Override
	public Event<RegistryEvents.EntryAdded<V>> quilt$getEntryAddedEvent() {
		return this.quilt$entryAddedEvent;
	}
}
