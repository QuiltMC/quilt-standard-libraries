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

import org.quiltmc.qsl.registry.api.sync.RegistrySynchronization;

/**
 * Utility class to build a new {@link Registry}.
 *
 * @param <T> the entry type tracked by this registry
 */
public final class QuiltRegistryBuilder<T> {
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

	private final RegistryKey<Registry<T>> key;
	private Lifecycle lifecycle;
	private Function<T, Holder.Reference<T>> customHolderProvider;
	private Identifier defaultId;
	private SyncBehavior syncBehavior;

	/**
	 * Creates a new {@code QuiltRegistryBuilder}.
	 *
	 * @param id the identifier of the registry
	 */
	public QuiltRegistryBuilder(@NotNull Identifier id) {
		this.key = RegistryKey.ofRegistry(id);

		this.lifecycle = Lifecycle.stable();
		this.syncBehavior = SyncBehavior.SKIPPED;
	}

	/**
	 * Sets the lifecycle of the registry.
	 * <p>
	 * By default, this is {@linkplain Lifecycle#stable() stable}.
	 *
	 * @param lifecycle the new lifecycle
	 * @return this builder
	 */
	@Contract("_ -> this")
	public @NotNull QuiltRegistryBuilder<T> lifecycle(@NotNull Lifecycle lifecycle) {
		this.lifecycle = lifecycle;
		return this;
	}

	/**
	 * Sets the lifecycle of this registry to be stable.
	 *
	 * @return this builder
	 * @see #lifecycle(Lifecycle)
	 * @see Lifecycle#stable()
	 */
	@Contract("-> this")
	public @NotNull QuiltRegistryBuilder<T> stable() {
		return this.lifecycle(Lifecycle.stable());
	}

	/**
	 * Sets the lifecycle of this registry to be experimental.
	 *
	 * @return this builder
	 * @see #lifecycle(Lifecycle)
	 * @see Lifecycle#experimental()
	 */
	@Contract("-> this")
	public @NotNull QuiltRegistryBuilder<T> experimental() {
		return this.lifecycle(Lifecycle.experimental());
	}

	/**
	 * Sets the lifecycle of this registry to be deprecated.
	 *
	 * @param since the data version this registry has been deprecated since
	 * @return this builder
	 * @see #lifecycle(Lifecycle)
	 * @see Lifecycle#deprecated(int)
	 */
	@Contract("_ -> this")
	public @NotNull QuiltRegistryBuilder<T> deprecated(int since) {
		return this.lifecycle(Lifecycle.deprecated(since));
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
	@Contract("_ -> this")
	public @NotNull QuiltRegistryBuilder<T> customHolderProvider(@Nullable Function<T, Holder.Reference<T>> customHolderProvider) {
		this.customHolderProvider = customHolderProvider;
		return this;
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
	 *
	 * @apiNote The API does <em>not</em> check if {@code defaultId} is an existing entry in this registry!
	 */
	@Contract("_ -> this")
	public @NotNull QuiltRegistryBuilder<T> defaultId(@Nullable Identifier defaultId) {
		this.defaultId = defaultId;
		return this;
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
	public @NotNull QuiltRegistryBuilder<T> syncBehavior(@NotNull SyncBehavior syncBehavior) {
		this.syncBehavior = syncBehavior;
		return this;
	}

	/**
	 * Sets the registry to <em>not</em> be synchronized at all.
	 *
	 * @return this builder
	 * @see #syncBehavior(SyncBehavior)
	 * @see SyncBehavior#SKIPPED
	 */
	@Contract("-> this")
	public @NotNull QuiltRegistryBuilder<T> syncSkipped() {
		return this.syncBehavior(SyncBehavior.SKIPPED);
	}

	/**
	 * Sets the registry to be synchronized, and to be required - clients who do not have this registry on their side
	 * <em>will</em> be kicked.
	 *
	 * @return this builder
	 * @see #syncBehavior(SyncBehavior)
	 * @see SyncBehavior#REQUIRED
	 */
	@Contract("-> this")
	public @NotNull QuiltRegistryBuilder<T> syncRequired() {
		return this.syncBehavior(SyncBehavior.REQUIRED);
	}

	/**
	 * Sets the registry to be synchronized, and to be optional - clients who do not have this registry on their side
	 * <em>will not</em> be kicked.
	 *
	 * @return this builder.
	 * @see #syncBehavior(SyncBehavior)
	 * @see SyncBehavior#OPTIONAL
	 */
	@Contract("-> this")
	public @NotNull QuiltRegistryBuilder<T> syncOptional() {
		return this.syncBehavior(SyncBehavior.OPTIONAL);
	}

	/**
	 * Builds the {@code Registry} instance.
	 *
	 * @return the newly constructed registry
	 */
	@Contract("-> new")
	@SuppressWarnings("unchecked")
	public @NotNull SimpleRegistry<T> build() {
		SimpleRegistry<T> registry;
		if (this.defaultId == null) {
			registry = new SimpleRegistry<>(this.key, this.lifecycle, this.customHolderProvider);
		} else {
			// this takes the identifier as a string, to guarantee that it's unique
			registry = new DefaultedRegistry<>(this.defaultId.toString(), this.key, this.lifecycle, this.customHolderProvider);
		}
		Registry.register((Registry<Registry<Object>>) Registry.REGISTRIES, this.key.getValue(), (Registry<Object>) registry);

		if (this.syncBehavior == SyncBehavior.REQUIRED || this.syncBehavior == SyncBehavior.OPTIONAL) {
			RegistrySynchronization.markForSync(registry);
			if (this.syncBehavior == SyncBehavior.OPTIONAL) {
				RegistrySynchronization.setRegistryOptional(registry);
			}
		}

		return registry;
	}
}
