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

package org.quiltmc.qsl.registry.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;

import org.quiltmc.qsl.registry.api.sync.RegistrySynchronization;

/**
 * Utility class to build a new built-in {@link Registry}.
 *
 * @param <T> the entry type tracked by this registry
 */
public final class QuiltBuiltinRegistryBuilder<T> extends QuiltRegistryBuilder<T, QuiltBuiltinRegistryBuilder<T>> {
	/**
	 * Specifies the behavior for synchronizing the registry and its contents.
	 */
	public enum SyncBehavior {
		/**
		 * The registry <em>will not</em> be synchronized to the client.
		 */
		SKIPPED,
		/**
		 * The registry <em>will</em> be synchronized to the client,
		 * and clients who do not have this registry on their side <em>will</em> be kicked.
		 */
		REQUIRED,
		/**
		 * The registry <em>will</em> be synchronized to the client,
		 * and clients who do not have this registry on their side <em>will not</em> be kicked.
		 */
		OPTIONAL
	}

	private SyncBehavior syncBehavior;

	/**
	 * Creates a new {@code QuiltRegistryBuilder}.
	 *
	 * @param id the identifier of the registry
	 */
	QuiltBuiltinRegistryBuilder(@NotNull Identifier id) {
		super(id);

		this.syncBehavior = SyncBehavior.SKIPPED;
	}

	/**
	 * Sets the synchronization behavior of this registry.
	 * <p>
	 * By default, this is {@link SyncBehavior#SKIPPED}.
	 *
	 * @param syncBehavior the new synchronization behavior
	 * @return this builder
	 */
	@Contract("_ -> this")
	public @NotNull QuiltBuiltinRegistryBuilder<T> withSyncBehavior(@NotNull SyncBehavior syncBehavior) {
		this.syncBehavior = syncBehavior;
		return this;
	}

	/**
	 * Sets the registry to <em>not</em> be synchronized at all.
	 *
	 * @return this builder
	 * @see #withSyncBehavior(SyncBehavior)
	 * @see SyncBehavior#SKIPPED
	 */
	@Contract("-> this")
	public @NotNull QuiltBuiltinRegistryBuilder<T> syncSkipped() {
		return this.withSyncBehavior(SyncBehavior.SKIPPED);
	}

	/**
	 * Sets the registry to be synchronized, and to be required - clients who do not have this registry on their side
	 * <em>will</em> be kicked.
	 *
	 * @return this builder
	 * @see #withSyncBehavior(SyncBehavior)
	 * @see SyncBehavior#REQUIRED
	 */
	@Contract("-> this")
	public @NotNull QuiltBuiltinRegistryBuilder<T> syncRequired() {
		return this.withSyncBehavior(SyncBehavior.REQUIRED);
	}

	/**
	 * Sets the registry to be synchronized, and to be optional - clients who do not have this registry on their side
	 * <em>will not</em> be kicked.
	 *
	 * @return this builder.
	 * @see #withSyncBehavior(SyncBehavior)
	 * @see SyncBehavior#OPTIONAL
	 */
	@Contract("-> this")
	public @NotNull QuiltBuiltinRegistryBuilder<T> syncOptional() {
		return this.withSyncBehavior(SyncBehavior.OPTIONAL);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void onRegistryBuilt(SimpleRegistry<T> registry) {
		super.onRegistryBuilt(registry);

		Registry.register((Registry<Registry<Object>>) Registry.REGISTRIES, this.key.getValue(), (Registry<Object>) registry);

		if (this.syncBehavior == SyncBehavior.REQUIRED || this.syncBehavior == SyncBehavior.OPTIONAL) {
			RegistrySynchronization.markForSync(registry);
			if (this.syncBehavior == SyncBehavior.OPTIONAL) {
				RegistrySynchronization.setRegistryOptional(registry);
			}
		}
	}
}
