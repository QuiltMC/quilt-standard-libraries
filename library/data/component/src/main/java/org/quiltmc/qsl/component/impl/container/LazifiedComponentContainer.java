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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentContainer;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.components.NbtComponent;
import org.quiltmc.qsl.component.api.event.ComponentEvents;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.util.ErrorUtil;
import org.quiltmc.qsl.component.impl.util.Lazy;
import org.quiltmc.qsl.component.impl.util.StringConstants;

import java.util.*;

public class LazifiedComponentContainer implements ComponentContainer {

	private final Map<Identifier, Lazy<Component>> components;
	private final Set<Identifier> nbtComponents;
	@Nullable
	private final Runnable saveOperation;

	protected LazifiedComponentContainer(@NotNull ComponentProvider provider, @Nullable Runnable saveOperation) {
		this.components = this.initializeComponents(provider);
		this.nbtComponents = new HashSet<>(this.components.size());
		this.saveOperation = saveOperation;
	}

	private Map<Identifier, Lazy<Component>> initializeComponents(ComponentProvider provider) {
		var map = new HashMap<Identifier, Lazy<Component>>();
		ComponentsImpl.get(provider).forEach(type -> map.put(type.id(), this.createLazy(type)));
		ComponentEvents.INJECT.invoker().onInject(provider, type -> map.put(type.id(), this.createLazy(type)));
		return map;
	}

	private Lazy<Component> createLazy(ComponentType<?> type) {
		return Lazy.of(() -> {
			Component component = type.create();
			if (component instanceof NbtComponent<?> nbtComponent) {
				this.nbtComponents.add(type.id());
				nbtComponent.setSaveOperation(this.saveOperation);
			}

			return component;
		});
	}

	public static <T> Optional<LazifiedComponentContainer.Builder> builder(T obj) {
		if (!(obj instanceof ComponentProvider provider)) {
			return Optional.empty();
		}

		return Optional.of(new Builder(provider));
	}

	@Override
	public Optional<Component> expose(Identifier id) {
		return Optional.ofNullable(this.components.get(id)).map(Lazy::get);
	}

	@Override
	public Map<Identifier, Component> exposeAll() {
		return this.components.entrySet().stream()
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().get()), HashMap::putAll);
	}

	@Override
	public void writeNbt(@NotNull NbtCompound providerRootNbt) {
		var rootQslNbt = new NbtCompound();
		this.nbtComponents.forEach(id -> this.components.get(id).ifPresent(component ->
				NbtComponent.writeTo(rootQslNbt, (NbtComponent<?>) component, id)
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
				.filter(Objects::nonNull)
				.forEach(id -> this.expose(id).ifPresent(component -> {
					if (component instanceof NbtComponent<?> nbtComponent) {
						NbtComponent.readFrom(nbtComponent, id, rootQslNbt);
					}
				}));
	}

	@Override
	public void moveComponents(ComponentContainer other) {
		if (other instanceof LazifiedComponentContainer otherContainer) {
			otherContainer.components.forEach((id, componentLazy) -> componentLazy.ifPresent(component -> {
				this.components.put(id, componentLazy); // Directly overriding our value.

				if (component instanceof NbtComponent<?> nbtComponent) {
					this.nbtComponents.add(id);
					nbtComponent.setSaveOperation(this.saveOperation);
				}
			}));

			otherContainer.components.clear();
			otherContainer.nbtComponents.clear();
		} else {
			throw ErrorUtil.illegalArgument("Cannot move components from a non-lazified container to one that is!").get();
		}
	}

	public static class Builder {

		private final ComponentProvider provider;
		private Runnable saveOperation;

		private Builder(ComponentProvider provider) {
			this.provider = provider;
		}

		public Builder setSaveOperation(Runnable runnable) {
			this.saveOperation = runnable;
			return this;
		}

		public LazifiedComponentContainer build() {
			return new LazifiedComponentContainer(this.provider, this.saveOperation);
		}
	}
}
