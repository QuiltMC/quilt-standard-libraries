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
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.api.component.NbtSerializable;
import org.quiltmc.qsl.component.api.component.Syncable;
import org.quiltmc.qsl.component.api.component.Tickable;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.api.sync.SyncChannel;
import org.quiltmc.qsl.component.api.injection.ComponentEntry;
import org.quiltmc.qsl.component.impl.util.ErrorUtil;
import org.quiltmc.qsl.component.impl.util.StringConstants;

import java.util.*;
import java.util.function.Function;

public abstract class AbstractComponentContainer implements ComponentContainer {
	protected final ContainerOperations operations;
	protected final List<ComponentType<?>> nbtComponents;
	protected final Maybe<List<ComponentType<?>>> ticking;
	protected final Maybe<Queue<ComponentType<?>>> pendingSync;
	protected final Maybe<SyncChannel<?, ?>> syncContext;

	public AbstractComponentContainer(@Nullable Runnable saveOperation,
			boolean ticking,
			@Nullable SyncChannel<?, ?> syncChannel) {
		this.ticking = ticking ? Maybe.just(new ArrayList<>()) : Maybe.nothing();
		this.nbtComponents = new ArrayList<>();
		this.syncContext = Maybe.wrap(syncChannel);
		this.pendingSync = this.syncContext.map(it -> new ArrayDeque<>());
		this.operations = new ContainerOperations(
				saveOperation,
				type -> () -> this.pendingSync.ifJust(pending -> pending.add(type))
		);
	}

	@Override
	public void writeNbt(NbtCompound providerRootNbt) {
		var rootQslNbt = providerRootNbt.getCompound(StringConstants.COMPONENT_ROOT);
		this.nbtComponents.forEach(type -> this.expose(type)
											   .map(it -> ((NbtSerializable<?>) it))
											   .ifJust(nbtComponent -> NbtSerializable.writeTo(
													   rootQslNbt, nbtComponent, type.id()))
		);

		if (!rootQslNbt.isEmpty()) {
			providerRootNbt.put(StringConstants.COMPONENT_ROOT, rootQslNbt);
		}
	}

	@Override
	public void readNbt(NbtCompound providerRootNbt) {
		var rootQslNbt = providerRootNbt.getCompound(StringConstants.COMPONENT_ROOT);

		rootQslNbt.getKeys().stream()
				  .map(Identifier::new) // All encoded component types *must* strictly be identifiers
				  .map(Components.REGISTRY::get)
				  .filter(Objects::nonNull)
				  .forEach(type -> this.expose(type)
									   .map(component -> ((NbtSerializable<?>) component))
									   .ifJust(component -> NbtSerializable.readFrom(component, type.id(), rootQslNbt)));
	}

	@Override
	public void tick(ComponentProvider provider) {
		this.ticking.ifJust(componentTypes -> componentTypes.forEach(type -> this.expose(type)
																				 .map(it -> ((Tickable) it))
																				 .ifJust(tickingComponent -> tickingComponent.tick(
																						 provider)))
		).ifNothing(() -> {
			throw ErrorUtil.illegalState("Attempted to tick a non-ticking container").get();
		});

		this.sync(provider);
	}

	@Override
	public void sync(ComponentProvider provider) {
		this.syncContext.ifJust(channel -> channel.syncFromQueue(
				this.pendingSync.unwrap(),
				type -> (Syncable) this.expose(type).unwrap(),
				provider
		)).ifNothing(() -> {
			throw ErrorUtil.illegalState("Attempted to sync a non-syncable container!").get();
		});
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

		this.ticking.ifJust(componentTypes -> {
			if (component instanceof Tickable) {
				componentTypes.add(type);
			}
		});

		this.addComponent(type, component);

		return component;
	}

	public record ContainerOperations(@Nullable Runnable saveOperation,
									  @Nullable Function<ComponentType<?>, Runnable> syncOperationFactory) { }
}
