package org.quiltmc.qsl.component.impl.container;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.*;
import org.quiltmc.qsl.component.api.components.NbtComponent;
import org.quiltmc.qsl.component.api.components.SyncedComponent;
import org.quiltmc.qsl.component.api.components.TickingComponent;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.injection.ComponentEntry;
import org.quiltmc.qsl.component.impl.sync.packet.SyncPacket;
import org.quiltmc.qsl.component.impl.util.ErrorUtil;
import org.quiltmc.qsl.component.impl.util.StringConstants;

import java.util.*;
import java.util.function.Function;

public abstract class AbstractComponentContainer implements ComponentContainer {
	protected final ContainerOperations operations;
	protected final List<ComponentType<?>> nbtComponents;
	protected final Maybe<List<ComponentType<?>>> ticking;
	protected final Maybe<Queue<ComponentType<?>>> pendingSync;
	protected final Maybe<SyncPacket.SyncContext> syncContext;

	public AbstractComponentContainer(Runnable saveOperation,
									  boolean ticking,
									  @Nullable SyncPacket.SyncContext syncContext) {
		this.ticking = ticking ? Maybe.just(new ArrayList<>()) : Maybe.nothing();
		this.nbtComponents = new ArrayList<>();
		this.syncContext = Maybe.wrap(syncContext);
		this.pendingSync = this.syncContext.map(it -> new ArrayDeque<>());
		this.operations = new ContainerOperations(
				saveOperation,
				type -> () -> this.pendingSync.ifJust(pending -> pending.add(type))
		);
	}

	@Override
	public void writeNbt(NbtCompound providerRootNbt) {
		var rootQslNbt = new NbtCompound();
		this.nbtComponents.forEach(type -> this.expose(type)
				.map(it -> ((NbtComponent<?>) it))
				.ifJust(nbtComponent -> NbtComponent.writeTo(rootQslNbt, nbtComponent, type.id()))
		);

		if (!rootQslNbt.isEmpty()) {
			providerRootNbt.put(StringConstants.COMPONENT_ROOT, rootQslNbt);
		}
	}

	@Override
	public void readNbt(NbtCompound providerRootNbt) {
		var rootQslNbt = providerRootNbt.getCompound(StringConstants.COMPONENT_ROOT);

		rootQslNbt.getKeys().stream()
				.map(Identifier::new)
				.map(Components.REGISTRY::get)
				.filter(Objects::nonNull)
				.forEach(type -> this.expose(type)
						.map(component -> ((NbtComponent<?>) component))
						.ifJust(component -> NbtComponent.readFrom(component, type.id(), rootQslNbt))
				);
	}

	@Override
	public void tick(ComponentProvider provider) {
		this.ticking.ifJust(componentTypes -> componentTypes.forEach(type ->
				this.expose(type)
						.map(it -> ((TickingComponent) it))
						.ifJust(tickingComponent -> tickingComponent.tick(provider)))
		).ifNothing(() -> {
			throw ErrorUtil.illegalState("Attempted to tick a non-ticking container").get();
		});

		this.sync(provider);
	}

	@Override
	public void sync(ComponentProvider provider) {
		this.syncContext.ifJust(ctx -> SyncPacket.syncFromQueue(
				this.pendingSync.unwrap(),
				ctx,
				type -> (SyncedComponent) this.expose(type).unwrap(),
				provider
		)).ifNothing(() -> {
			throw ErrorUtil.illegalState("Attempted to sync a non-syncable container!").get();
		});
	}

	protected abstract <COMP extends Component> void addComponent(ComponentType<COMP> type, Component component);

	protected <COMP extends Component> COMP initializeComponent(ComponentEntry<COMP> componentEntry) {
		ComponentType<?> type = componentEntry.type();
		Runnable syncOperation = this.operations.syncOperationFactory().apply(type);

		COMP component = componentEntry.apply(this.operations.saveOperation(), syncOperation);

		if (component instanceof NbtComponent<?>) {
			this.nbtComponents.add(type);
		}

		this.ticking.ifJust(componentTypes -> {
			if (component instanceof TickingComponent) {
				componentTypes.add(type);
			}
		});

		this.addComponent(type, component);

		return component;
	}

	public record ContainerOperations(Runnable saveOperation,
									  Function<ComponentType<?>, Runnable> syncOperationFactory) {
	}
}
