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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.api.component.NbtSerializable;
import org.quiltmc.qsl.component.api.component.Tickable;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.injection.ComponentEntry;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.api.sync.SyncChannel;
import org.quiltmc.qsl.component.impl.util.StringConstants;

public abstract class AbstractComponentContainer implements ComponentContainer {
	protected final ContainerOperations operations;
	protected final List<ComponentType<?>> nbtComponents;
	@Nullable
	protected final List<ComponentType<?>> ticking;
	@Nullable
	protected final Queue<ComponentType<?>> pendingSync;
	@Nullable
	protected final SyncChannel<?, ?> syncContext;

	public AbstractComponentContainer(@Nullable Runnable saveOperation,
			boolean ticking,
			@Nullable SyncChannel<?, ?> syncChannel) {
		this.ticking = ticking ? new ArrayList<>() : null;
		this.nbtComponents = new ArrayList<>();
		this.syncContext = syncChannel;
		this.pendingSync = this.syncContext != null ? new ArrayDeque<>() : null;
		this.operations = new ContainerOperations(
				saveOperation,
				type -> () -> {
					if (this.pendingSync != null) {
						this.pendingSync.add(type);
					}
				}
		);
	}

	@Override
	public void writeNbt(NbtCompound providerRootNbt) {
		var rootQslNbt = providerRootNbt.getCompound(StringConstants.COMPONENT_ROOT);

		for (ComponentType<?> type : this.nbtComponents) {
			Object current = this.expose(type);
			NbtSerializable.writeTo(rootQslNbt, (NbtSerializable<?>) current, type.id());
		}

		if (!rootQslNbt.isEmpty()) {
			providerRootNbt.put(StringConstants.COMPONENT_ROOT, rootQslNbt);
		}
	}

	@Override
	public void readNbt(NbtCompound providerRootNbt) {
		var rootQslNbt = providerRootNbt.getCompound(StringConstants.COMPONENT_ROOT);

		for (String nbtKey : rootQslNbt.getKeys()) {
			var id = new Identifier(nbtKey); // All keys *must* be identifiers
			var type = Components.REGISTRY.get(id);

			if (type != null) {
				Object current = this.expose(type);
				NbtSerializable.readFrom(rootQslNbt, (NbtSerializable<?>) current, id);
			}
		}
	}

	@Override
	public void tick(ComponentProvider provider) {
		if (this.ticking != null) {
			for (ComponentType<?> type : this.ticking) {
				Object current = this.expose(type);
				((Tickable) current).tick(provider);
			}

			this.sync(provider);
		}
	}

	@Override
	public void sync(ComponentProvider provider) {
		if (this.syncContext != null) {
			this.syncContext.syncFromQueue(this.pendingSync, provider);
		} else {
			throw new UnsupportedOperationException("Attempted to sync a non-syncable container!");
		}
	}

	protected abstract <COMP> void addComponent(ComponentType<COMP> type, COMP component);

	protected <COMP> COMP initializeComponent(ComponentEntry<COMP> componentEntry) {
		ComponentType<COMP> type = componentEntry.type();

		Function<ComponentType<?>, Runnable> factory = this.operations.syncOperationFactory();
		Runnable syncOperation = factory != null ? factory.apply(type) : null;

		COMP component = componentEntry.apply(this.operations.saveOperation(), syncOperation);

		if (component instanceof NbtSerializable<?>) {
			this.nbtComponents.add(type);
		}

		if (this.ticking != null) {
			this.ticking.add(type);
		}

		this.addComponent(type, component);

		return component;
	}

	public record ContainerOperations(@Nullable Runnable saveOperation,
									  @Nullable Function<ComponentType<?>, Runnable> syncOperationFactory) { }
}
