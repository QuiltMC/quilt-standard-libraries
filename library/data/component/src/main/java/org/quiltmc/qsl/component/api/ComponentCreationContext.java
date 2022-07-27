package org.quiltmc.qsl.component.api;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.component.api.component.NbtSerializable;
import org.quiltmc.qsl.component.api.component.Syncable;

/**
 * A list of methods a component may call when needed to interact with outside factors.<br/>
 * The <i>outside factors</i> statement refers to things like the
 * {@link org.quiltmc.qsl.component.api.container.ComponentContainer}
 * or the {@link org.quiltmc.qsl.component.api.provider.ComponentProvider} this component belongs to.
 *
 * @author 0xJoeMama
 */
@SuppressWarnings("ClassCanBeRecord")// we want the class to be extendable to people can create their own Contexes
public class ComponentCreationContext {
	private final @Nullable Runnable saveOperation;
	private final @Nullable Runnable syncOperation;

	/**
	 * Both of these parameters may be <code>null</code>, depending on the container this component belongs to.
	 *
	 * @param saveOperation The action performed to cause an {@link NbtSerializable} to issue a save to its
	 *                      {@linkplain org.quiltmc.qsl.component.api.provider.ComponentProvider provider}.
	 * @param syncOperation The action performed to cause a {@link Syncable} to issue a sync to its
	 *                      {@linkplain org.quiltmc.qsl.component.api.container.ComponentContainer container}.
	 */
	public ComponentCreationContext(@Nullable Runnable saveOperation, @Nullable Runnable syncOperation) {
		this.saveOperation = saveOperation;
		this.syncOperation = syncOperation;
	}

	public @Nullable Runnable saveOperation() {
		return this.saveOperation;
	}

	public @Nullable Runnable syncOperation() {
		return this.syncOperation;
	}
}
