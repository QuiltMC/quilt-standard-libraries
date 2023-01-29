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
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtCompound;

import org.quiltmc.qsl.base.api.util.Lazy;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.component.NbtSerializable;
import org.quiltmc.qsl.component.api.component.Tickable;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.injection.ComponentEntry;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.api.sync.SyncChannel;

public class SingleComponentContainer<T> implements ComponentContainer {
	private final ComponentType<T> type;
	@Nullable
	private final SyncChannel<?, ?> syncChannel;
	private final boolean ticking;
	private Lazy<? extends T> entry;
	private boolean shouldSync = false;

	protected SingleComponentContainer(ComponentType<T> type, boolean ticking,
			@Nullable SyncChannel<?, ?> syncChannel) {
		this.type = type;
		this.ticking = ticking;
		this.syncChannel = syncChannel;
	}

	public static <C> ComponentContainer.Factory<SingleComponentContainer<C>> createFactory(ComponentEntry<C> entry) {
		return (provider, ignored, saveOperation, ticking, syncChannel) -> {
			ComponentType<C> type = entry.type();
			var container = new SingleComponentContainer<>(type, ticking, syncChannel);
			container.setEntry(() -> entry.apply(saveOperation, () -> container.shouldSync = true));

			return container;
		};
	}

	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public <C> C expose(ComponentType<C> type) {
		return type == this.type ? (C) this.entry.get() : null;
	}

	@Override
	public void writeNbt(NbtCompound providerRootNbt) {
		this.entry.ifFilled(c -> {
			if (c instanceof NbtSerializable<?> nbtSerializable) {
				NbtSerializable.writeTo(providerRootNbt, nbtSerializable, this.type.id());
			}
		});
	}

	@Override
	public void readNbt(NbtCompound providerRootNbt) {
		String idString = this.type.id().toString();
		if (providerRootNbt.getKeys().contains(idString)) {
			T component = this.expose(this.type);
			if (component instanceof NbtSerializable<?> nbtComponent) {
				NbtSerializable.readFrom(providerRootNbt, nbtComponent, this.type.id());
			}
		}
	}

	@Override
	public void tick(ComponentProvider provider) {
		if (this.ticking) {
			T obj = this.expose(this.type);
			if (obj == null) {
				throw new UnsupportedOperationException("Attempted to tick a non-tickable container!");
			}

			((Tickable) obj).tick(provider);
		}
	}

	@Override
	public void sync(ComponentProvider provider) {
		if (this.syncChannel != null) {
			if (this.shouldSync) {
				this.entry.ifFilled(
						t -> this.syncChannel.syncFromQueue(new ArrayDeque<>(List.of(this.type)), provider));
			}
		} else {
			throw new UnsupportedOperationException("Attempted to sync a non-syncable container!");
		}
	}

	@Override
	public void forEach(BiConsumer<ComponentType<?>, ? super Object> action) {
		if (this.entry.isFilled()) {
			action.accept(this.type, this.entry.get());
		}
	}

	private void setEntry(Supplier<? extends T> supplier) {
		this.entry = Lazy.of(supplier);
	}
}
