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

import java.util.function.Function;

import com.mojang.serialization.Lifecycle;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Holder;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.DefaultedRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

/**
 * Utility class to build a new {@link Registry}.
 *
 * @param <T>    the entry type tracked by this registry
 * @param <SELF> the type of the builder
 */
public abstract class QuiltRegistryBuilder<T, SELF extends QuiltRegistryBuilder<T, SELF>> {
	protected final RegistryKey<Registry<T>> key;
	protected Lifecycle lifecycle;
	protected Function<T, Holder.Reference<T>> customHolderProvider;
	protected Identifier defaultId;

	/**
	 * Creates a new built-in {@code Registry} builder.
	 *
	 * @param id the identifier of the registry
	 * @return the newly created builder
	 * @param <T> the entry type tracked by this registry
	 */
	@Contract("_ -> new")
	public static <T> QuiltBuiltinRegistryBuilder<T> builtin(@NotNull Identifier id) {
		return new QuiltBuiltinRegistryBuilder<>(id);
	}

	/**
	 * Creates a new {@code QuiltRegistryBuilder}.
	 *
	 * @param id the identifier of the registry
	 */
	protected QuiltRegistryBuilder(@NotNull Identifier id) {
		this.key = RegistryKey.ofRegistry(id);

		this.lifecycle = Lifecycle.stable();
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
	 * Sets the custom {@link Holder} provider of this registry.
	 * <p>
	 * This should be used if your registry entry needs to store its own holder. Add the following to your registry entry class:
	 * <pre><code>
	 * public class Thing {
	 *     private final Holder<Thing> registryHolder = YourCoolModRegistries.THINGS.createIntrusiveHolder(this);
	 *
	 *     public Holder<Thing> getRegistryHolder() {
	 *         return this.registryHolder;
	 *     }
	 * }
	 * </code></pre>
	 * Then, set this to call that {@code getRegistryHolder()} method:
	 * <pre><code>
	 * public class YourCoolModRegistries {
	 *     public static final SimpleRegistry<Thing> THINGS =
	 *         new QuiltRegistryBuilder(YourCoolMod.id("things"))
	 *             .customHolderProvider(Thing::getRegistryHolder)
	 *             .build();
	 * }
	 * </code></pre>
	 * <p>
	 * By default, this is {@code null} - the registry will create and maintain its own holders.
	 *
	 * @param customHolderProvider the new custom holder provider
	 * @return this builder
	 */
	@SuppressWarnings("unchecked")
	@Contract("_ -> this")
	public @NotNull SELF withCustomHolderProvider(@Nullable Function<T, Holder.Reference<T>> customHolderProvider) {
		this.customHolderProvider = customHolderProvider;
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
	 * Called when a registry is built via {@link #build()}.
	 *
	 * @param registry the newly built registry
	 */
	protected void onRegistryBuilt(SimpleRegistry<T> registry) {
		// TODO boostrap
	}

	/**
	 * Builds the {@code Registry} instance.
	 *
	 * @return the newly constructed registry
	 */
	@Contract("-> new")
	public @NotNull SimpleRegistry<T> build() {
		SimpleRegistry<T> registry;
		if (this.defaultId == null) {
			registry = new SimpleRegistry<>(this.key, this.lifecycle, this.customHolderProvider);
		} else {
			// this takes the identifier as a string, to guarantee that it's unique
			registry = new DefaultedRegistry<>(this.defaultId.toString(), this.key, this.lifecycle, this.customHolderProvider);
		}

		this.onRegistryBuilt(registry);

		return registry;
	}
}
