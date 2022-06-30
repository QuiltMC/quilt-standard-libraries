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

package org.quiltmc.qsl.component.impl.container;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.*;
import org.quiltmc.qsl.component.api.components.NbtComponent;
import org.quiltmc.qsl.component.api.components.SyncedComponent;
import org.quiltmc.qsl.component.api.components.TickingComponent;
import org.quiltmc.qsl.component.api.event.ComponentEvents;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.sync.header.SyncPacketHeader;
import org.quiltmc.qsl.component.impl.sync.packet.SyncPacket;
import org.quiltmc.qsl.component.impl.util.ErrorUtil;
import org.quiltmc.qsl.component.impl.util.Lazy;
import org.quiltmc.qsl.component.impl.util.StringConstants;

import java.util.*;
import java.util.function.Supplier;

public class LazyComponentContainer implements ComponentContainer {
	private final IdentityHashMap<ComponentType<?>, Lazy<Component>> components;
	private final List<ComponentType<?>> nbtComponents;
	private final List<ComponentType<?>> tickingComponents;
	@Nullable
	private final Runnable saveOperation;
	private final boolean ticking;
	private final boolean syncing;
	private final SyncPacket.SyncContext syncContext;
	private final Queue<ComponentType<?>> pendingSync;

	protected LazyComponentContainer(
			@NotNull ComponentProvider provider,
			@Nullable Runnable saveOperation,
			boolean ticking,
			@Nullable SyncPacket.SyncContext syncContext
	) {
		this.saveOperation = saveOperation;
		this.ticking = ticking;
		this.syncing = syncContext != null;
		this.syncContext = syncContext;
		this.nbtComponents = new ArrayList<>();
		this.tickingComponents = new ArrayList<>();
		this.pendingSync = new ArrayDeque<>();
		this.components = this.initializeComponents(provider);
	}

	public static <T> Optional<LazyComponentContainer.Builder> builder(T obj) {
		if (!(obj instanceof ComponentProvider provider)) {
			return Optional.empty();
		}

		return Optional.of(new Builder(provider));
	}

	public static void move(@NotNull LazyComponentContainer from, @NotNull LazyComponentContainer into) {
		from.components.forEach((id, componentLazy) -> componentLazy.ifPresent(component -> {
			into.components.put(id, componentLazy); // Directly overriding our value.

			if (component instanceof NbtComponent<?> nbtComponent) {
				into.nbtComponents.add(id);
				nbtComponent.setSaveOperation(into.saveOperation);
			}
		}));

		from.components.clear();
		from.nbtComponents.clear();
	}

	@Override
	public Optional<Component> expose(ComponentType<?> id) {
		return Optional.ofNullable(this.components.get(id)).map(Lazy::get);
	}

	@Override
	public void writeNbt(@NotNull NbtCompound providerRootNbt) {
		var rootQslNbt = new NbtCompound();
		this.nbtComponents.forEach(type -> this.components.get(type).ifPresent(component ->
				NbtComponent.writeTo(rootQslNbt, (NbtComponent<?>) component, type.id())
		));

		if (!rootQslNbt.isEmpty()) {
			providerRootNbt.put(StringConstants.COMPONENT_ROOT, rootQslNbt);
		}
	}

	@Override
	public void readNbt(@NotNull NbtCompound providerRootNbt) {
		var rootQslNbt = providerRootNbt.getCompound(StringConstants.COMPONENT_ROOT);

		rootQslNbt.getKeys().stream()
				.map(Identifier::tryParse)
				.filter(Objects::nonNull)// TODO: Looks a bit suspicous...
				.map(Components.REGISTRY::get)
				.filter(Objects::nonNull)
				.forEach(type -> this.expose(type).ifPresent(component -> {
					if (component instanceof NbtComponent<?> nbtComponent) {
						NbtComponent.readFrom(nbtComponent, type.id(), rootQslNbt);
					}
				}));
	}

	@Override
	public void tick(@NotNull ComponentProvider provider) {
		if (!this.ticking) {
			throw ErrorUtil.runtime("Attempted to tick a non-tickable ComponentContainer instance").get();
		}

		this.tickingComponents.stream()
				.map(this::expose)
				.map(Optional::orElseThrow)
				.map(it -> ((TickingComponent) it))
				.forEach(tickingComponent -> tickingComponent.tick(provider));

		// Sync any queued components
		if (this.syncing) {
			this.sync(provider);
		}
	}

	@Override
	public void receiveSyncPacket(@NotNull ComponentType<?> type, @NotNull PacketByteBuf buf) {
		this.expose(type).map(it -> ((SyncedComponent) it)).ifPresent(syncedComponent -> syncedComponent.readFromBuf(buf));
		ComponentsImpl.LOGGER.info("Received packet for {}", type);
	}

	@Override
	public void sync(@NotNull ComponentProvider provider) {
		var map = new HashMap<ComponentType<?>, SyncedComponent>();

		while (!this.pendingSync.isEmpty()) {
			var id = this.pendingSync.poll();
			map.put(id, (SyncedComponent) this.components.get(id).get());
		}

		if (!map.isEmpty()) {
			SyncPacket.send(this.syncContext, provider, map);
		}
	}

	private IdentityHashMap<ComponentType<?>, Lazy<Component>> initializeComponents(ComponentProvider provider) {
		var map = new IdentityHashMap<ComponentType<?>, Lazy<Component>>();
		ComponentsImpl.getInjections(provider).forEach(type -> map.put(type, this.createLazy(type)));
		ComponentEvents.DYNAMIC_INJECT.invoker().onInject(provider, type -> map.put(type, this.createLazy(type)));
		return map;
	}

	private Lazy<Component> createLazy(ComponentType<?> type) {
		if (type.isStatic()) {
			Component singleton = type.create();
			if (this.ticking && singleton instanceof TickingComponent) {
				this.tickingComponents.add(type);
			}

			return Lazy.filled(singleton);
		}

		return Lazy.of(() -> {
			Component component = type.create();
			if (component instanceof NbtComponent<?> nbtComponent) {
				this.nbtComponents.add(type);
				nbtComponent.setSaveOperation(this.saveOperation);
			}

			if (this.ticking && component instanceof TickingComponent) {
				this.tickingComponents.add(type);
			}

			if (this.syncing && component instanceof SyncedComponent syncedComponent) {
				syncedComponent.setSyncOperation(() -> this.pendingSync.add(type));
			}

			return component;
		});
	}

	public static class Builder {

		private final ComponentProvider provider;
		@Nullable
		private Runnable saveOperation;
		private boolean ticking;
		private SyncPacket.SyncContext syncContext;

		private Builder(ComponentProvider provider) {
			this.provider = provider;
			this.ticking = false;
			this.saveOperation = null;
		}

		public Builder setSaveOperation(Runnable runnable) {
			this.saveOperation = runnable;
			return this;
		}

		public Builder ticking() {
			this.ticking = true;
			return this;
		}

		public Builder syncing(@NotNull SyncPacketHeader<?> header, Supplier<Collection<ServerPlayerEntity>> playerGenerator) {
			this.syncContext = new SyncPacket.SyncContext(header, playerGenerator);
			return this;
		}

		public LazyComponentContainer build() {
			return new LazyComponentContainer(this.provider, this.saveOperation, this.ticking, this.syncContext);
		}
	}
}
