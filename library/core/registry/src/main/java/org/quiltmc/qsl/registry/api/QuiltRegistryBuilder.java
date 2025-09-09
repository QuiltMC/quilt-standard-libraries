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

import com.mojang.serialization.Lifecycle;
import net.minecraft.registry.DefaultMappedRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Utility class to build a new {@link Registry}.
 *
 * @param <T>    the entry type tracked by this registry
 * @param <SELF> the type of the builder
 */
public abstract class QuiltRegistryBuilder<T, SELF extends QuiltRegistryBuilder<T, SELF>> {
	protected final RegistryKey<Registry<T>> key;
	protected Lifecycle lifecycle;
	protected boolean useIntrusiveHolders;
	protected Consumer<Registry<T>> bootstrap;
	protected Identifier defaultId;
	protected RegistrySynchronizationBehavior syncBehavior;

	/**
	 * Creates a new built-in {@code Registry} builder.
	 *
	 * @param key the key of the registry
	 * @param <T> the entry type tracked by this registry
	 * @return the newly created builder
	 */
	@Contract("_ -> new")
	public static <T> QuiltBuiltinRegistryBuilder<T> builtin(@NotNull RegistryKey<Registry<T>> key) {
		return new QuiltBuiltinRegistryBuilder<>(key);
	}

	/**
	 * Creates a new {@code QuiltRegistryBuilder}.
	 *
	 * @param key the key of the registry
	 */
	protected QuiltRegistryBuilder(@NotNull RegistryKey<Registry<T>> key) {
		this.key = key;

		this.lifecycle = Lifecycle.stable();
		this.syncBehavior = RegistrySynchronizationBehavior.SKIPPED;
	}

	/**
	 * Sets the lifecycle of the registry.
	 * <p>
	 * By default, this is {@linkplain Lifecycle#stable() stable}.
	 *
	 * @param lifecycle the new lifecycle
	 * @return this builder
	 */
	@SuppressWarnings("unchecked")
	@Contract("_ -> this")
	public @NotNull SELF withLifecycle(@NotNull Lifecycle lifecycle) {
		this.lifecycle = lifecycle;
		return (SELF) this;
	}

	/**
	 * Sets the lifecycle of this registry to be stable.
	 *
	 * @return this builder
	 * @see #withLifecycle(Lifecycle)
	 * @see Lifecycle#stable()
	 */
	@Contract("-> this")
	public @NotNull SELF stable() {
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
	public @NotNull SELF experimental() {
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
	public @NotNull SELF deprecated(int since) {
		return this.withLifecycle(Lifecycle.deprecated(since));
	}

	/**
	 * Enables the use of the {@link Registry#createIntrusiveHolder(Object)} method for this registry.
	 * <p>
	 * By default, this is disabled.
	 *
	 * @return this builder
	 */
	@SuppressWarnings("unchecked")
	@Contract("-> this")
	public @NotNull SELF withIntrusiveHolders() {
		this.useIntrusiveHolders = true;
		return (SELF) this;
	}

	/**
	 * Sets this registry's <em>bootstrap method</em>, that will be called with the registry instance
	 * once it is {@linkplain #build() built}.
	 * <p>
	 * By default, this is {@code null}.
	 *
	 * @param bootstrap the new bootstrap method
	 * @return this builder
	 */
	@SuppressWarnings("unchecked")
	@Contract("_ -> this")
	public @NotNull SELF withBootstrap(Consumer<Registry<T>> bootstrap) {
		this.bootstrap = bootstrap;
		return (SELF) this;
	}

	/**
	 * Sets the default identifier of this registry.
	 * <p>
	 * Should a nonexistent entry be referenced in some way, the registry will instead reference the entry identified
	 * by this instead.
	 * <p>
	 * By default, this is {@code null} - the registry will simply return {@code null} when a nonexistent entry is referenced.
	 *
	 * @param defaultId the new default identifier
	 * @return this builder
	 */
	@SuppressWarnings("unchecked")
	@Contract("_ -> this")
	public @NotNull SELF withDefaultId(@Nullable Identifier defaultId) {
		this.defaultId = defaultId;
		return (SELF) this;
	}

	/**
	 * Sets the synchronization behavior of this registry.
	 * <p>
	 * By default, this is {@link RegistrySynchronizationBehavior#SKIPPED}.
	 *
	 * @param syncBehavior the new synchronization behavior
	 * @return this builder
	 */
	@SuppressWarnings("unchecked")
	@Contract("_ -> this")
	public @NotNull SELF withSyncBehavior(@NotNull RegistrySynchronizationBehavior syncBehavior) {
		this.syncBehavior = syncBehavior;
		return (SELF) this;
	}

	/**
	 * Sets the registry to <em>not</em> be synchronized at all.
	 *
	 * @return this builder
	 * @see #withSyncBehavior(RegistrySynchronizationBehavior)
	 * @see RegistrySynchronizationBehavior#SKIPPED
	 */
	@Contract("-> this")
	public @NotNull SELF withSyncSkipped() {
		return this.withSyncBehavior(RegistrySynchronizationBehavior.SKIPPED);
	}

	/**
	 * Sets the registry to be synchronized, and to be required - clients who do not have this registry on their side
	 * <em>will</em> be kicked.
	 *
	 * @return this builder
	 * @see #withSyncBehavior(RegistrySynchronizationBehavior)
	 * @see RegistrySynchronizationBehavior#REQUIRED
	 */
	@Contract("-> this")
	public @NotNull SELF withSyncRequired() {
		return this.withSyncBehavior(RegistrySynchronizationBehavior.REQUIRED);
	}

	/**
	 * Sets the registry to be synchronized, and to be optional - clients who do not have this registry on their side
	 * <em>will not</em> be kicked.
	 *
	 * @return this builder.
	 * @see #withSyncBehavior(RegistrySynchronizationBehavior)
	 * @see RegistrySynchronizationBehavior#OPTIONAL
	 */
	@Contract("-> this")
	public @NotNull SELF withSyncOptional() {
		return this.withSyncBehavior(RegistrySynchronizationBehavior.OPTIONAL);
	}

	/**
	 * Called when a registry is built via {@link #build()}.
	 *
	 * @param registry the newly built registry
	 */
	protected void onRegistryBuilt(SimpleRegistry<T> registry) {
		if (this.bootstrap != null) {
			this.bootstrap.accept(registry);
		}
	}

	/**
	 * Builds the {@code Registry} instance.
	 *
	 * @return the newly constructed registry
	 */
	@Contract("-> new")
	public @NotNull Registry<T> build() {
		SimpleRegistry<T> registry;
		if (this.defaultId == null) {
			registry = new SimpleRegistry<>(this.key, this.lifecycle, this.useIntrusiveHolders);
		} else {
			// this takes the identifier as a string, to guarantee that it's unique
			registry = new DefaultMappedRegistry<>(this.defaultId.toString(), this.key, this.lifecycle, this.useIntrusiveHolders);
		}

		this.onRegistryBuilt(registry);

		return registry;
	}
}

