package org.quiltmc.qsl.component.impl;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.Component;
import org.quiltmc.qsl.component.api.ComponentContainer;
import org.quiltmc.qsl.component.api.components.NbtComponent;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;
import org.quiltmc.qsl.component.impl.util.StringConstants;

import java.util.*;
import java.util.stream.Stream;

public class SimpleComponentContainer implements ComponentContainer {
	private final Map<Identifier, Component> components;
	private final List<Identifier> nbtComponents;

	public static @NotNull SimpleComponentContainer create(Runnable saveOperation, ComponentIdentifier<?>... ids) {
		return new SimpleComponentContainer(saveOperation, Stream.of(ids).map(ComponentIdentifier::id));
	}

	private SimpleComponentContainer(@Nullable Runnable saveOperation, Stream<Identifier> componentIds) {
		this.components = new HashMap<>();
		this.nbtComponents = new ArrayList<>();

		componentIds.forEach(id -> {
			Component component = ComponentsImpl.getEntry(id).get();
			this.components.put(id, component);

			if (component instanceof NbtComponent<?> nbtComponent) {
				this.nbtComponents.add(id);
				nbtComponent.setSaveOperation(saveOperation);
			}
		});

		componentIds.close();
	}

	@Override
	public Optional<Component> expose(Identifier id) {
		return Optional.ofNullable(this.components.get(id));
	}

	@Override
	public Map<Identifier, Component> exposeAll() {
		return this.components;
	}

	@Override
	public void moveComponents(ComponentContainer other) {
		throw new IllegalStateException("Cannot move components into a SimpleComponentContainer");
	}

	@Override
	public void writeNbt(NbtCompound providerRootNbt) {
		var rootQslNbt = new NbtCompound();
		this.nbtComponents.forEach(id -> this.expose(id).ifPresent(component -> NbtComponent.writeTo(rootQslNbt, (NbtComponent<?>) component, id)));
		if (!rootQslNbt.isEmpty()) {
			providerRootNbt.put(StringConstants.COMPONENT_ROOT, rootQslNbt);
		}
	}

	@Override
	public void readNbt(NbtCompound providerRootNbt) {
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
	public void setSaveOperation(@NotNull Runnable runnable) {
		throw new IllegalStateException("Cannot change the save operation of a SimpleComponentContainer since it needs to be passed into the constructor");
	}
}
