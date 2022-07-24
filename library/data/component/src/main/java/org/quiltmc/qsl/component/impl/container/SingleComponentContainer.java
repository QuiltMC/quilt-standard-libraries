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
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.util.Lazy;
import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.component.NbtSerializable;
import org.quiltmc.qsl.component.api.component.Syncable;
import org.quiltmc.qsl.component.api.component.Tickable;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.api.sync.SyncChannel;
import org.quiltmc.qsl.component.impl.injection.ComponentEntry;

import java.util.ArrayDeque;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class SingleComponentContainer<T> implements ComponentContainer {
	private final ComponentType<T> type;
	private final Maybe<SyncChannel<?, ?>> syncChannel;
	private final boolean ticking;
	private Lazy<? extends T> entry;
	private boolean shouldSync = false;

	protected SingleComponentContainer(ComponentType<T> type, boolean ticking,
			@Nullable SyncChannel<?, ?> syncChannel) {
		this.type = type;
		this.ticking = ticking;
		this.syncChannel = Maybe.wrap(syncChannel);
	}

	public static <C> ComponentContainer.Factory<SingleComponentContainer<C>> createFactory(ComponentEntry<C> entry) {
		return (provider, ignored, saveOperation, ticking, syncChannel) -> {
			ComponentType<C> type = entry.type();
			var container = new SingleComponentContainer<>(type, ticking, syncChannel);
			container.setEntry(() -> entry.apply(saveOperation, () -> container.shouldSync = true));

			return container;
		};
	}

	@Override
	public <C> Maybe<C> expose(ComponentType<C> type) {
		return (type == this.type ? Maybe.just(this.entry.get()) : Maybe.nothing()).castUnchecked();
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
			this.expose(this.type)
				.map(it -> ((NbtSerializable<?>) it))
				.ifJust(nbtComponent -> NbtSerializable.readFrom(nbtComponent, this.type.id(), providerRootNbt));
		}
	}

	@Override
	public void tick(ComponentProvider provider) {
		if (this.ticking) {
			this.expose(this.type)
				.map(it -> ((Tickable) it))
				.ifJust(tickingComponent -> tickingComponent.tick(provider));
		}
	}

	@Override
	public void sync(ComponentProvider provider) {
		if (this.shouldSync) {
			this.syncChannel.ifJust(channel -> channel.syncFromQueue(
					new ArrayDeque<>(List.of(this.type)),
					type -> (Syncable) this.entry.get(),
					provider
			));
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
