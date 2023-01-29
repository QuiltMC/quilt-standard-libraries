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
@SuppressWarnings("ClassCanBeRecord") // we want the class to be extendable to people can create their own Contexts
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
