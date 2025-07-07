/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.entity.extensions.api;

import java.util.Collection;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

import org.quiltmc.qsl.entity.extensions.impl.TradeOfferInternals;

/**
 * Utilities to help with registration of trade offers.
 */
public final class TradeOfferHelper {
	private TradeOfferHelper() {
		throw new UnsupportedOperationException(
			TradeOfferHelper.class.getSimpleName() + " contains only static members"
		);
	}

	/**
	 * Registers offer factories for use by villagers.
	 *
	 * <p>This adds offers to the default (non-rebalanced) trades, but they may be used with the rebalanced trade
	 * experiment as well if no rebalanced trades override them.<br>
	 * To add separate offers for the rebalanced trade experiment, use
	 * {@link #addToExperimentalVillagerOfferPool(RegistryKey, int, TradeOffers.Factory...)}.
	 *
	 * <p>Below is an example of registering an offer factory to be added a blacksmith with a profession level of 3:
	 * <blockquote><pre>
	 * TradeOfferHelper.registerVillagerOffers(
	 * 	VillagerProfession.BLACKSMITH, 3,
	 * 	new CustomTradeFactory(...),
	 * 	new CustomTradeFactory(...)
	 * );
	 * </pre></blockquote>
	 *
	 * @param profession	the registry key of the villager profession to assign the trades to
	 * @param level			the profession level the villager must be to offer the trades; must not be negative
	 * @param factories		the offer factories to add; must not be empty
	 *
	 * @see #addToVillagerOfferPool(RegistryKey, int, Collection)
	 */
	public static synchronized void addToVillagerOfferPool(
			@NotNull
			RegistryKey<VillagerProfession> profession,
			int level,
			@NotNull
			TradeOffers.Factory... factories
	) {
		TradeOfferInternals.addToVillagerOfferPool(profession, level, factories);
	}

	/**
	 * @see #addToVillagerOfferPool(RegistryKey, int, TradeOffers.Factory...)
	 */
	public static synchronized void addToVillagerOfferPool(
			@NotNull
			RegistryKey<VillagerProfession> profession,
			int level,
			@NotNull
			Collection<TradeOffers.Factory> factories
	) {
		Objects.requireNonNull(factories, "factories must not be null");
		addToVillagerOfferPool(profession, level, factories.toArray(new TradeOffers.Factory[0]));
	}

	/**
	 * Registers offer factories for use by villagers when the rebalanced trade experiment is enabled.
	 *
	 * <p>Below is an example of registering an offer factory to be added a blacksmith with a profession level of 3:
	 * <blockquote><pre>
	 * TradeOfferHelper.registerRebalancedVillagerOffers(
	 * 	VillagerProfession.BLACKSMITH, 3,
	 * 	new CustomTradeFactory(...),
	 * 	new CustomTradeFactory(...)
	 * );
	 * </pre></blockquote>
	 *
	 * <p><strong>Experimental feature</strong>. This API may receive changes as necessary to adapt to further
	 * experiment changes.
	 *
	 * @param profession 	the registry key of the villager profession to assign the trades to
	 * @param level			the profession level the villager must be to offer the trades; must not be negative
	 * @param factories 	the offer factories to add; must not be empty
	 */
	@ApiStatus.Experimental
	public static synchronized void addToExperimentalVillagerOfferPool(
			@NotNull
			RegistryKey<VillagerProfession> profession,
			int level,
			@NotNull
			TradeOffers.Factory... factories
	) {
		TradeOfferInternals.addToExperimentalVillagerOfferPool(profession, level, factories);
	}

	/**
	 * @see #addToExperimentalVillagerOfferPool(RegistryKey, int, TradeOffers.Factory...)
	 */
	@ApiStatus.Experimental
	public static synchronized void addToExperimentalVillagerOfferPool(
			@NotNull
			RegistryKey<VillagerProfession> profession,
			int level,
			@NotNull
			Collection<TradeOffers.Factory> factories
	) {
		addToExperimentalVillagerOfferPool(profession, level, factories.toArray(new TradeOffers.Factory[0]));
	}

	/**
	 * Adds offer factories to the identified Wandering Trader offer {@code pool}.
	 *
	 * <p>Identifiers for vanilla's pools can be found in {@link VanillaWanderingTraderPoolIds}.<br>
	 * If the passed identifier hasn't been
	 * {@linkplain #registerWanderingTraderPool(Identifier, int, TradeOffers.Factory...) registered} yet,
	 * then the passed {@code factories} will only be added to the pool once it has been registered.
	 *
	 * @param id		the identifier of the offer pool
	 * @param factories the offer factories to; must not be empty
	 *
	 * @see #addToWanderingTraderOfferPool(Identifier, Collection)
	 */
	public static synchronized void addToWanderingTraderOfferPool(
			@NotNull
			Identifier id,
			@NotNull
			TradeOffers.Factory... factories
	) {
		TradeOfferInternals.addToWanderingTraderOfferPool(id, factories);
	}

	/**
	 * @see #addToWanderingTraderOfferPool(Identifier, TradeOffers.Factory...)
	 */
	public static synchronized void addToWanderingTraderOfferPool(
			Identifier pool, Collection<TradeOffers.Factory> factories
	) {
		TradeOfferInternals.addToWanderingTraderOfferPool(pool, factories.toArray(new TradeOffers.Factory[0]));
	}

	/**
	 * Registers a new Wandering Trader offer pool.
	 *
	 * @param id		the identifier of the offer pool
	 * @param count		the number of offers to select from the pool; must be greater than {@code 0}
	 * @param factories the offer factories that make up the pool; must not be empty
	 *
	 * @throws IllegalArgumentException if a pool with the passed {@code id} has already been registered
	 *
	 * @see #registerWanderingTraderPool(Identifier, int, Collection)
	 */
	public static synchronized void registerWanderingTraderPool(
			@NotNull
			Identifier id,
			int count,
			@NotNull
			TradeOffers.Factory... factories
	) {
		TradeOfferInternals.registerWanderingTraderPool(id, count, factories);
	}

	/**
	 * @see #registerWanderingTraderPool(Identifier, int, TradeOffers.Factory...)
	 */
	public static synchronized void registerWanderingTraderPool(
			@NotNull
			Identifier pool,
			int count,
			@NotNull
			Collection<TradeOffers.Factory> factories
	) {
		TradeOfferInternals.registerWanderingTraderPool(pool, count, factories.toArray(new TradeOffers.Factory[0]));
	}

	/**
	 * Identifiers for vanilla's Wandering Trader offer pools.
	 */
	public static final class VanillaWanderingTraderPoolIds {
		private VanillaWanderingTraderPoolIds() {
			throw new UnsupportedOperationException(
				VanillaWanderingTraderPoolIds.class.getSimpleName() + " contains only static members"
			);
		}

		/**
		 * The pool ID for the "buy items" pool.
		 * Two offers are picked from this pool.
		 *
		 * <p>In vanilla, this pool contains offers to buy water buckets, baked potatoes, etc.
		 * for emeralds.
		 */
		public static final Identifier BUY_ITEMS = Identifier.ofDefault("buy_items");
		/**
		 * The pool ID for the "sell special items" pool.
		 * Two offers are picked from this pool.
		 *
		 * <p>In vanilla, this pool contains offers to sell logs, enchanted iron pickaxes, etc.
		 */
		public static final Identifier SELL_SPECIAL_ITEMS = Identifier.ofDefault("sell_special_items");
		/**
		 * The pool ID for the "sell common items" pool.
		 * Five offers are picked from this pool.
		 *
		 * <p>In vanilla, this pool contains offers to sell flowers, saplings, etc.
		 */
		public static final Identifier SELL_COMMON_ITEMS = Identifier.ofDefault("sell_common_items");
	}
}
