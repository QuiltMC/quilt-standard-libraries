package org.quiltmc.qsl.component.impl;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
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
	private final List<Identifier> nbtComponents;
	@Nullable
	private Runnable saveOperation = null;

	private LazifiedComponentContainer(ComponentProvider provider) {
		this.components = ComponentProvider.createComponents(provider);
		this.nbtComponents = new ArrayList<>(components.size());
	}

	public static <T> Optional<LazifiedComponentContainer> create(T obj) {
		if (!(obj instanceof ComponentProvider provider)) {
			return Optional.empty();
		}

		return Optional.of(new LazifiedComponentContainer(provider));
	}

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

	public Map<Identifier, Component> exposeAll() {
		return this.components.entrySet().stream()
				.filter(entry -> !entry.getValue().isEmpty())
				.collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue().get()), HashMap::putAll);
	}

	public Map<Identifier, NbtComponent<?>> getNbtComponents() {
		return this.nbtComponents.stream()
				.map(id -> new Pair<>(id, this.components.get(id)))
				.filter(pair -> Objects.nonNull(pair.getRight()))
				.filter(pair -> pair.getRight().isEmpty())
				.collect(HashMap::new, (map, pair) -> map.put(pair.getLeft(), (NbtComponent<?>) pair.getRight().get()), HashMap::putAll);
	}

	public void setSaveOperation(@NotNull Runnable runnable) {
		this.saveOperation = runnable;
	}

	public void writeNbt(@NotNull NbtCompound providerRootNbt) {
		var rootQslNbt = new NbtCompound();
		this.nbtComponents.forEach(id -> this.components.get(id).ifPresent(component ->
				NbtComponent.writeTo(rootQslNbt, (NbtComponent<?>) component, id)
		));

		if (!rootQslNbt.isEmpty()) {
			providerRootNbt.put(StringConstants.COMPONENT_ROOT, rootQslNbt);
		}
	}

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

	public void moveComponents(ComponentContainer other) {
		if (other instanceof LazifiedComponentContainer otherContainer) {
			otherContainer.components.forEach((id, componentLazy) -> {
				if (!componentLazy.isEmpty()) {
					this.components.put(id, componentLazy);

					if (componentLazy.get() instanceof NbtComponent<?> nbtComponent) {
						this.nbtComponents.add(id);
						nbtComponent.setSaveOperation(this.saveOperation);
					}
				}
			});

			otherContainer.components.clear();
			otherContainer.nbtComponents.clear();
		} else {
			throw new IllegalArgumentException("Cannot move components from a non-lazified container to one that is!");
		}
	}
}
