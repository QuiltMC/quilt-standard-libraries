package org.quiltmc.qsl.component.impl;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentContainer;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.components.NbtComponent;
import org.quiltmc.qsl.component.impl.util.Lazy;
import org.quiltmc.qsl.component.impl.util.StringConstants;

import java.util.*;

public class LazifiedComponentContainer implements ComponentContainer {

	private final Map<Identifier, Lazy<Component>> components;
	private final Set<Identifier> nbtComponents;
	@Nullable
	private Runnable saveOperation = null;

	protected LazifiedComponentContainer(ComponentProvider provider) {
		this.components = createComponents(provider);
		this.nbtComponents = new HashSet<>(components.size());
	}

	public static <T> Optional<LazifiedComponentContainer> create(T obj) {
		if (!(obj instanceof ComponentProvider provider)) {
			return Optional.empty();
		}

		return Optional.of(new LazifiedComponentContainer(provider));
	}

	public static @NotNull Map<Identifier, Lazy<Component>> createComponents(@NotNull ComponentProvider provider) {
		var map = new HashMap<Identifier, Lazy<Component>>();
		ComponentsImpl.get(provider).forEach((identifier, supplier) -> map.put(identifier, Lazy.of(supplier::get)));
		return map;
	}

	@Override
	public Optional<Component> expose(Identifier id) {
		return Optional.ofNullable(this.components.get(id))
				.map(componentLazy -> {
					if (componentLazy.isEmpty() && componentLazy.get() instanceof NbtComponent<?> nbtComponent) {
						this.nbtComponents.add(id);
						nbtComponent.setSaveOperation(this.saveOperation);
						return nbtComponent;
					}

					return componentLazy.get();
				});
	}

	@Override
	public Map<Identifier, Component> exposeAll() {
		return this.components.entrySet().stream()
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().get()), HashMap::putAll);
	}

	/*
	public Map<Identifier, NbtComponent<?>> getNbtComponents() {
		return this.nbtComponents.stream()
				.map(id -> new Pair<>(id, this.components.get(id)))
				.filter(pair -> Objects.nonNull(pair.getRight()))
				.filter(pair -> pair.getRight().isEmpty())
				.collect(HashMap::new, (map, pair) -> map.put(pair.getLeft(), (NbtComponent<?>) pair.getRight().get()), HashMap::putAll);
	}
	*/

	@Override
	public void setSaveOperation(@NotNull Runnable runnable) {
		this.saveOperation = runnable;
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
			throw new IllegalArgumentException("Cannot move components from a non-lazified container to one that is!");
		}
	}
}
