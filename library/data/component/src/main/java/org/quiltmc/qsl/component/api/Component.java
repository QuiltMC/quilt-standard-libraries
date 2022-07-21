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

/**
 * Marks a type as a container for data and/or behaviour.
 *
 * @author 0xJoeMama
 */
public interface Component {
	/**
	 * Class meant to provide a component using the provided {@link Operations} argument.
	 *
	 * @param <T> The type of the returned {@link Component}.
	 * @author 0xJoeMama
	 */
	@FunctionalInterface
	interface Factory<T extends Component> {
		/**
		 * @param operations The {@link Operations} that the {@link Component} may use.
		 * @return A {@link T} instance.
		 */
		T create(Component.Operations operations);
	}

	/**
	 * A list of method a {@link Component} may call when needed to interact with outside factors.<br/>
	 * The <i>outside factors</i> statement refers to things like the {@link org.quiltmc.qsl.component.api.container.ComponentContainer}
	 * or the {@link org.quiltmc.qsl.component.api.provider.ComponentProvider} this {@link Component} belongs to.
	 *
	 * @author 0xJoeMama
	 */
	@SuppressWarnings("ClassCanBeRecord") // we want the class to be extendable to people can create their own Operations
	class Operations {
		private final @Nullable Runnable saveOperation;
		private final @Nullable Runnable syncOperation;

		/**
		 * Both of these parameters may be <code>null</code>, depending on the container this {@link Component} belongs to.
		 *
		 * @param saveOperation The action performed to cause an {@link org.quiltmc.qsl.component.api.component.NbtComponent} to issue a save to its {@linkplain org.quiltmc.qsl.component.api.provider.ComponentProvider provider}.
		 * @param syncOperation The action performed to cause a {@link org.quiltmc.qsl.component.api.component.SyncedComponent} to issue a sync to its {@linkplain org.quiltmc.qsl.component.api.container.ComponentContainer container}.
		 */
		public Operations(@Nullable Runnable saveOperation, @Nullable Runnable syncOperation) {
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
}
