package org.quiltmc.qsl.component.impl.container;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.*;
import org.quiltmc.qsl.component.api.components.NbtComponent;
import org.quiltmc.qsl.component.api.components.SyncedComponent;
import org.quiltmc.qsl.component.api.components.TickingComponent;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.sync.packet.SyncPacket;
import org.quiltmc.qsl.component.impl.util.StringConstants;

import java.util.*;
import java.util.function.Predicate;

// Suggestion from Technici4n from fabric. May help improve performance and memory footprint once done.
public class OnAccessComponentContainer implements ComponentContainer {
	private final Map<ComponentType<?>, Component> components;
	private final Set<ComponentType<?>> supportedTypes;
	private final Set<ComponentType<?>> tickingComponents;
	private final Set<ComponentType<?>> nbtComponents;
	@Nullable
	private final Runnable saveOperation;
	@Nullable
	private final Queue<ComponentType<?>> pendingSync;
	@Nullable
	private final SyncPacket.SyncContext syncContext;

	private OnAccessComponentContainer(@NotNull ComponentProvider provider, @Nullable Runnable saveOperation, @Nullable SyncPacket.SyncContext syncContext) {
		this.syncContext = syncContext;
		this.saveOperation = saveOperation;
		this.nbtComponents = new HashSet<>();
		this.tickingComponents = new HashSet<>();
		this.pendingSync = this.syncContext != null ? new ArrayDeque<>() : null;
		this.supportedTypes = ComponentsImpl.getInjections(provider);
		this.components = this.getInitialComponents();
	}

	@Override
	public Optional<Component> expose(ComponentType<?> type) {
		return Optional.ofNullable(this.components.get(type))
				.or(() -> this.supportedTypes.contains(type) ? Optional.of(this.createComponent(type)) : Optional.empty());
	}

	@Override
	public void writeNbt(@NotNull NbtCompound providerRootNbt) {
		if (this.nbtComponents.isEmpty()) {
			return;
		}

		var rootQslNbt = new NbtCompound();

		this.nbtComponents.forEach(type -> {
			var component = (NbtComponent<?>) this.components.get(type);
			NbtComponent.writeTo(rootQslNbt, component, type.id());
		});

		if (!rootQslNbt.isEmpty()) {
			providerRootNbt.put(StringConstants.COMPONENT_ROOT, rootQslNbt);
		}
	}

	@Override
	public void readNbt(@NotNull NbtCompound providerRootNbt) {
		if (providerRootNbt.isEmpty() || !providerRootNbt.contains(StringConstants.COMPONENT_ROOT)) {
			return;
		}

		var rootQslNbt = providerRootNbt.getCompound(StringConstants.COMPONENT_ROOT);

		rootQslNbt.getKeys().stream()
				.map(Identifier::tryParse)
				.filter(Objects::nonNull)
				.forEach(id -> this.expose(Components.REGISTRY.get(id))
						.map(it -> ((NbtComponent<?>) it))
						.ifPresent(nbtComponent -> NbtComponent.readFrom(nbtComponent, id, rootQslNbt))
				);
	}

	@Override
	public void tick(@NotNull ComponentProvider provider) {
		this.tickingComponents.stream()
				.map(this.components::get)
				.map(it -> ((TickingComponent) it))
				.forEach(tickingComponent -> tickingComponent.tick(provider));
	}

	@Override
	public void sync(@NotNull ComponentProvider provider) {
		if (this.syncContext != null) {
			// pendingsync will be null if synccontext is null and the other way around
			//noinspection ConstantConditions
			SyncPacket.syncFromQueue(
					this.pendingSync,
					this.syncContext,
					type -> ((SyncedComponent) this.components.get(type)),
					provider
			);
		}
	}

	private Map<ComponentType<?>, Component> getInitialComponents() {
		return this.supportedTypes.stream() // TODO: We can cache this value.
				.filter(((Predicate<ComponentType<?>>) ComponentType::isStatic).or(ComponentType::isInstant))
				.collect(IdentityHashMap::new, (map, type) -> map.put(type, this.createComponent(type)), Map::putAll);
	}

	private Component createComponent(@NotNull ComponentType<?> type) {
		var component = type.create();
		if (component instanceof NbtComponent<?> nbtComponent) {
			this.nbtComponents.add(type);
			nbtComponent.setSaveOperation(this.saveOperation);
		}

		if (component instanceof TickingComponent) {
			this.tickingComponents.add(type);
		}

		if (component instanceof SyncedComponent synced && this.syncContext != null) {
			// pendingsync will be null if synccontext is null and the other way around
			//noinspection ConstantConditions
			synced.setSyncOperation(() -> this.pendingSync.add(type));
		}

		return component;
	}
}
