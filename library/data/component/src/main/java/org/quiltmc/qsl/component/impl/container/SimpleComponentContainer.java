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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.*;
import org.quiltmc.qsl.component.api.components.NbtComponent;
import org.quiltmc.qsl.component.api.components.SyncedComponent;
import org.quiltmc.qsl.component.api.components.TickingComponent;
import org.quiltmc.qsl.component.impl.sync.header.SyncPacketHeader;
import org.quiltmc.qsl.component.impl.sync.packet.SyncPacket;
import org.quiltmc.qsl.component.impl.util.ErrorUtil;
import org.quiltmc.qsl.component.impl.util.StringConstants;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class SimpleComponentContainer implements ComponentContainer {
	private final IdentityHashMap<ComponentType<?>, Component> components;
	private final List<ComponentType<?>> nbtComponents;
	private final List<ComponentType<?>> tickingComponents;
	@Nullable
	private final SyncPacket.SyncContext syncContext;
	private Queue<ComponentType<?>> pendingSync;

	protected SimpleComponentContainer(@Nullable Runnable saveOperation, SyncPacket.@Nullable SyncContext syncContext, Stream<ComponentType<?>> types) {
		this.components = new IdentityHashMap<>();
		this.nbtComponents = new ArrayList<>();
		this.tickingComponents = new ArrayList<>();
		if (syncContext != null) {
			this.syncContext = syncContext;
			this.pendingSync = new ArrayDeque<>();
		} else {
			this.syncContext = null;
		}

		types.forEach(type -> this.initializeComponent(saveOperation, type));
		types.close();
	}

	@Contract("-> new")
	@NotNull
	public static Builder builder() {
		return new Builder();
	}

	@Override
	public Optional<Component> expose(ComponentType<?> type) {
		return Optional.ofNullable(this.components.get(type));
	}

	@Override
	public void writeNbt(@NotNull NbtCompound providerRootNbt) {
		var rootQslNbt = new NbtCompound();
		this.nbtComponents.forEach(id -> this.expose(id)
				.ifPresent(component -> NbtComponent.writeTo(rootQslNbt, (NbtComponent<?>) component, id.id()))
		);

		if (!rootQslNbt.isEmpty()) {
			providerRootNbt.put(StringConstants.COMPONENT_ROOT, rootQslNbt);
		}
	}

	@Override
	public void readNbt(@NotNull NbtCompound providerRootNbt) {
		var rootQslNbt = providerRootNbt.getCompound(StringConstants.COMPONENT_ROOT);

		if (rootQslNbt.isEmpty()) {
			return;
		}

		rootQslNbt.getKeys().stream()
				.map(Identifier::tryParse)
				.filter(Objects::nonNull)
				.map(Components.REGISTRY::get)
				.filter(Objects::nonNull)
				.forEach(id -> this.expose(id)
						.ifPresent(component -> NbtComponent.readFrom((NbtComponent<?>) component, id.id(), rootQslNbt))
				);
	}

	@Override
	public void tick(@NotNull ComponentProvider provider) {
		this.tickingComponents.stream()
				.map(this.components::get)
				.map(it -> ((TickingComponent) it))
				.forEach(tickingComponent -> tickingComponent.tick(provider));

		this.sync(provider);
	}

	@Override
	public void receiveSyncPacket(@NotNull ComponentType<?> type, @NotNull PacketByteBuf buf) {
		((SyncedComponent) this.components.get(type)).readFromBuf(buf);
	}

	@Override
	public void sync(@NotNull ComponentProvider provider) {
		if (this.syncContext == null) {
			throw ErrorUtil.illegalState("Cannot sync a non-syncable component container! Make sure you provide a context!").get();
		}
		var map = new HashMap<ComponentType<?>, SyncedComponent>();

		while (!this.pendingSync.isEmpty()) {
			var currentType = this.pendingSync.poll();
			map.put(currentType, (SyncedComponent) this.components.get(currentType));
		}

		if (!map.isEmpty()) {
			SyncPacket.send(this.syncContext, provider, map);
		}
	}

	private void initializeComponent(@Nullable Runnable saveOperation, ComponentType<?> type) {
		Component component = type.create();
		this.components.put(type, component);

		if (component instanceof NbtComponent<?> nbtComponent) {
			this.nbtComponents.add(type);
			nbtComponent.setSaveOperation(saveOperation);
		}

		if (component instanceof TickingComponent) {
			this.tickingComponents.add(type);
		}

		if (component instanceof SyncedComponent synced) {
			synced.setSyncOperation(() -> this.pendingSync.add(type));
		}
	}

	public static class Builder {
		@NotNull
		private final List<ComponentType<?>> types;
		@Nullable
		private Runnable saveOperation;
		@Nullable
		private SyncPacket.SyncContext syncContext;

		private Builder() {
			this.types = new ArrayList<>();
		}

		@NotNull
		public Builder setSaveOperation(@NotNull Runnable runnable) {
			this.saveOperation = runnable;
			return this;
		}

		@NotNull
		public Builder syncing(SyncPacketHeader<?> header, Supplier<Collection<ServerPlayerEntity>> supplier) {
			this.syncContext = new SyncPacket.SyncContext(header, supplier);
			return this;
		}

		@NotNull
		public Builder add(ComponentType<?> type) {
			this.types.add(type);
			return this;
		}

		@NotNull
		public Builder add(ComponentType<?>... types) {
			this.types.addAll(Arrays.asList(types));
			return this;
		}

		@NotNull
		public SimpleComponentContainer build() {
			return new SimpleComponentContainer(this.saveOperation, this.syncContext, this.types.stream());
		}
	}
}
