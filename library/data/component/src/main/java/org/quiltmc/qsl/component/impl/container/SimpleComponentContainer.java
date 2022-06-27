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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentContainer;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.components.NbtComponent;
import org.quiltmc.qsl.component.api.components.TickingComponent;
import org.quiltmc.qsl.component.impl.util.StringConstants;

import java.util.*;
import java.util.stream.Stream;

public class SimpleComponentContainer implements ComponentContainer {
	private final Map<Identifier, Component> components;
	private final List<Identifier> nbtComponents;
	private final List<Identifier> tickingComponents;

	protected SimpleComponentContainer(@Nullable Runnable saveOperation, Stream<ComponentType<?>> types) {
		this.components = new HashMap<>();
		this.nbtComponents = new ArrayList<>();
		this.tickingComponents = new ArrayList<>();

		types.forEach(type -> {
			Component component = type.create();
			this.components.put(type.id(), component);

			if (component instanceof NbtComponent<?> nbtComponent) {
				this.nbtComponents.add(type.id());
				nbtComponent.setSaveOperation(saveOperation);
			}

			if (component instanceof TickingComponent) {
				this.tickingComponents.add(type.id());
			}
		});

		types.close();
	}

	@Contract("-> new")
	@NotNull
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		@NotNull
		private final List<ComponentType<?>> types;
		@Nullable
		private Runnable saveOperation;

		private Builder() {
			this.types = new ArrayList<>();
		}

		@NotNull
		public Builder setSaveOperation(@NotNull Runnable runnable) {
			this.saveOperation = runnable;
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

		public SimpleComponentContainer build() {
			return new SimpleComponentContainer(this.saveOperation, this.types.stream());
		}
	}	@Override
	public Optional<Component> expose(Identifier id) {
		return Optional.ofNullable(this.components.get(id));
	}

	@Override
	public void moveComponents(ComponentContainer other) {
		throw new IllegalStateException("Cannot move components into a SimpleComponentContainer");
	}

	@Override
	public void writeNbt(@NotNull NbtCompound providerRootNbt) {
		var rootQslNbt = new NbtCompound();
		this.nbtComponents.forEach(id -> this.expose(id).ifPresent(component -> NbtComponent.writeTo(rootQslNbt, (NbtComponent<?>) component, id)));
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
				.forEach(id -> this.expose(id).ifPresent(component -> NbtComponent.readFrom((NbtComponent<?>) component, id, rootQslNbt)));
	}

	@Override
	public void tick(@NotNull ComponentProvider provider) {
		this.tickingComponents.stream()
				.map(this.components::get)
				.map(it -> ((TickingComponent) it))
				.forEach(tickingComponent -> tickingComponent.tick(provider));
	}
}
