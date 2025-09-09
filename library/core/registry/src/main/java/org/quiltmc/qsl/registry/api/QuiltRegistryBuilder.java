/*
 * Copyright 2022 The Quilt Project
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

import java.util.function.Consumer;

import com.mojang.serialization.Lifecycle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.DefaultMappedRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.api.sync.RegistrySynchronization;

/**
 * Utility class to build a new code-driven {@link Registry}.
 *
 * <p>For data-driven registries, see {@link org.quiltmc.qsl.registry.api.dynamic.DynamicMetaRegistry}.
 *
 * @param <T>    the entry type tracked by this registry
 */
public final class QuiltRegistryBuilder<T> {
	private final RegistryKey<Registry<T>> key;
	private Lifecycle lifecycle;
	private boolean useIntrusiveHolders;
	private Consumer<Registry<T>> bootstrap;
	private Identifier defaultId;
	private SyncBehavior syncBehavior;

	/**
	 * Creates a new {@code QuiltRegistryBuilder}.
	 *
	 * @param key the key of the registry
	 * @param <T> the entry type tracked by this registry
	 * @return the newly created builder
	 */
	@Contract(value = "_ -> new", pure = true)
	public static <T> @NotNull QuiltRegistryBuilder<T> of(@NotNull RegistryKey<Registry<T>> key) {
		return new QuiltRegistryBuilder<>(key);
	}

	private QuiltRegistryBuilder(@NotNull RegistryKey<Registry<T>> key) {
		this.key = key;

		this.lifecycle = Lifecycle.stable();
		this.syncBehavior = SyncBehavior.SKIPPED;
	}

	/**
	 * Sets the lifecycle of the registry.
	 *
	 * <p>By default, this is {@linkplain Lifecycle#stable() stable}.
	 *
	 * @param lifecycle the new lifecycle
	 * @return this builder
	 */
	@Contract("_ -> this")
	public @NotNull QuiltRegistryBuilder<T> withLifecycle(@NotNull Lifecycle lifecycle) {
		this.lifecycle = lifecycle;
		return this;
	}

	/**
	 * Sets the lifecycle of this registry to be stable.
	 *
	 * @return this builder
	 * @see #withLifecycle(Lifecycle)
	 * @see Lifecycle#stable()
	 */
	@Contract("-> this")
	public @NotNull QuiltRegistryBuilder<T> stable() {
		return this.withLifecycle(Lifecycle.stable());
	}

	/**
	 * Sets the lifecycle of this registry to be experimental.
	 *
	 * @return this builder
	 * @see #withLifecycle(Lifecycle)
	 * @see Lifecycle#experimental()
	 */
	@Contract("-> this")
	public @NotNull QuiltRegistryBuilder<T> experimental() {
		return this.withLifecycle(Lifecycle.experimental());
	}

	/**
	 * Sets the lifecycle of this registry to be deprecated.
	 *
	 * @param since the data version this registry has been deprecated since
	 * @return this builder
	 * @see #withLifecycle(Lifecycle)
	 * @see Lifecycle#deprecated(int)
	 */
	@Contract("_ -> this")
	public @NotNull QuiltRegistryBuilder<T> deprecated(int since) {
		return this.withLifecycle(Lifecycle.deprecated(since));
	}

	/**
	 * Enables the use of the {@link Registry#createIntrusiveHolder(Object)} method for this registry.
	 *
	 * <p>By default, this is disabled.
	 *
	 * @return this builder
	 */
	@Contract("-> this")
	public @NotNull QuiltRegistryBuilder<T> withIntrusiveHolders() {
		this.useIntrusiveHolders = true;
		return this;
	}

	/**
	 * Sets this registry's <em>bootstrap method</em>, that will be called with the registry instance
	 * once it is {@linkplain #build() built}.
	 *
	 * <p>By default, this is {@code null}.
	 *
	 * @param bootstrap the new bootstrap method
	 * @return this builder
	 */
	@Contract("_ -> this")
	public @NotNull QuiltRegistryBuilder<T> withBootstrap(Consumer<Registry<T>> bootstrap) {
		this.bootstrap = bootstrap;
		return this;
	}

	/**
	 * Sets the default identifier of this registry.
	 *
	 * <p>Should a nonexistent entry be referenced in some way, the registry will instead
	 * reference the entry identified by this.
	 *
	 * <p>By default, this is {@code null} - the registry will simply return {@code null}
	 * when a nonexistent entry is referenced.
	 *
	 * @param defaultId the new default identifier
	 * @return this builder
	 */
	@Contract("_ -> this")
	public @NotNull QuiltRegistryBuilder<T> withDefaultId(@Nullable Identifier defaultId) {
		this.defaultId = defaultId;
		return this;
	}

	/**
	 * Sets the synchronization behavior of this registry.
	 *
	 * <p>By default, this is {@link SyncBehavior#SKIPPED}.
	 *
	 * @param syncBehavior the new synchronization behavior
	 * @return this builder
	 */
	@Contract("_ -> this")
	public @NotNull QuiltRegistryBuilder<T> withSyncBehavior(@NotNull QuiltRegistryBuilder.SyncBehavior syncBehavior) {
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
	public @NotNull QuiltRegistryBuilder<T> withSyncSkipped() {
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
	public @NotNull QuiltRegistryBuilder<T> withSyncRequired() {
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
	public @NotNull QuiltRegistryBuilder<T> withSyncOptional() {
		return this.withSyncBehavior(SyncBehavior.OPTIONAL);
	}

	/**
	 * Builds the {@code Registry} instance.
	 *
	 * @return the newly constructed registry
	 */
	@SuppressWarnings("unchecked")
	@Contract("-> new")
	public @NotNull Registry<T> build() {
		SimpleRegistry<T> registry;
		if (this.defaultId == null) {
			registry = new SimpleRegistry<>(this.key, this.lifecycle, this.useIntrusiveHolders);
		} else {
			// this takes the identifier as a string, to guarantee that it's unique
			registry = new DefaultMappedRegistry<>(this.defaultId.toString(), this.key, this.lifecycle, this.useIntrusiveHolders);
		}

		Registry.register((Registry<Registry<Object>>) Registries.ROOT, this.key.getValue(), (Registry<Object>) registry);

		if (this.syncBehavior != SyncBehavior.SKIPPED) {
			RegistrySynchronization.markForSync(registry);

			if (this.syncBehavior == SyncBehavior.OPTIONAL) {
				RegistrySynchronization.setRegistryOptional(registry);
			}
		}

		if (this.bootstrap != null) {
			this.bootstrap.accept(registry);
		}

		return registry;
	}

	/**
	 * Specifies the behavior for synchronizing a registry and its contents.
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
		 * but clients who do not have this registry on their side <em>will not</em> be kicked.
		 */
		OPTIONAL
	}
}

