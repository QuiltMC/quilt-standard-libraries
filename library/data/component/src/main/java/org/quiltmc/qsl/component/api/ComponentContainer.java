package org.quiltmc.qsl.component.api;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.quiltmc.qsl.component.api.components.NbtComponent;
import org.quiltmc.qsl.component.impl.util.Lazy;
import org.quiltmc.qsl.component.impl.util.StringConstants;
import org.quiltmc.qsl.component.impl.util.duck.NbtComponentProvider;

import java.util.*;

public class ComponentContainer implements ComponentProvider, NbtComponentProvider {
	private final Map<Identifier, Lazy<Component>> components;
	private final List<Identifier> nbtComponents;

	public static <T> Optional<ComponentContainer> create(T obj) {
		if (!(obj instanceof ComponentProvider provider)) {
			return Optional.empty();
		}

		return Optional.of(new ComponentContainer(provider));
	}

	private ComponentContainer(ComponentProvider provider) {
		this.components = ComponentProvider.createComponents(provider);
		this.nbtComponents = new ArrayList<>(components.size());
	}

	@Override
	public Optional<Component> expose(Identifier id) { // TODO: Check for nbt component
		return Optional.ofNullable(this.components.get(id))
				.map(componentLazy -> {
					if (componentLazy.isEmpty()) {
						this.nbtComponents.add(id);
						return componentLazy.get();
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

	public Optional<NbtCompound> writeNbt() {
		if (this.nbtComponents.isEmpty()) {
			return Optional.empty();
		}

		var rootQslNbt = new NbtCompound();
		this.nbtComponents.forEach(id -> this.components.get(id).computeIfPresent(component ->
				NbtComponent.writeTo(rootQslNbt, (NbtComponent<?>) component, id)
		));

		return rootQslNbt.isEmpty() ? Optional.empty() : Optional.of(rootQslNbt);
	}

	public void readNbt(NbtCompound nbt) {
		var rootQslNbt = nbt.getCompound(StringConstants.COMPONENT_ROOT);

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
	public Map<Identifier, NbtComponent<?>> getNbtComponents() {
		return this.nbtComponents.stream()
				.map(id -> new Pair<>(id, this.components.get(id)))
				.filter(pair -> Objects.nonNull(pair.getRight()))
				.filter(pair -> pair.getRight().isEmpty())
				.collect(HashMap::new, (map, pair) -> map.put(pair.getLeft(), (NbtComponent<?>) pair.getRight().get()), HashMap::putAll);
	}
}
