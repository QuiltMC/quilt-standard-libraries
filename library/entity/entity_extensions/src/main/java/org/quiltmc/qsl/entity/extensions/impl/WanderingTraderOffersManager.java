/*
 * Copyright 2025 The Quilt Project
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

package org.quiltmc.qsl.entity.extensions.impl;

import static org.quiltmc.qsl.entity.extensions.api.TradeOfferHelper.VanillaWanderingTraderPoolIds.BUY_ITEMS;
import static org.quiltmc.qsl.entity.extensions.api.TradeOfferHelper.VanillaWanderingTraderPoolIds.SELL_COMMON_ITEMS;
import static org.quiltmc.qsl.entity.extensions.api.TradeOfferHelper.VanillaWanderingTraderPoolIds.SELL_SPECIAL_ITEMS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.village.TradeOffers;

import org.quiltmc.qsl.entity.extensions.mixin.accessor.TradeOffersAccessor;

/**
 * Methods to help keep {@link TradeOffers#WANDERING_TRADER_TRADES} pool indexes and ids synchronized.
 */
@ApiStatus.Internal
public class WanderingTraderOffersManager {
	private WanderingTraderOffersManager() {
		throw new UnsupportedOperationException("WanderingTraderOffersManager contains only static members");
	}

	private static final Object2IntMap<Identifier> INDEXES_BY_ID = Util.make(new Object2IntOpenHashMap<>(), map -> {
		map.put(BUY_ITEMS, 0);
		map.put(SELL_SPECIAL_ITEMS, 1);
		map.put(SELL_COMMON_ITEMS, 2);
	});

	public static Optional<Pair<TradeOffers.Factory[], Integer>> getPool(Identifier id) {
		final int index = INDEXES_BY_ID.getOrDefault(id, -1);
		return index < 0 ? Optional.empty()
			: Optional.of(TradeOffers.WANDERING_TRADER_TRADES.get(index));
	}

	public static void setPool(Identifier id, Pair<TradeOffers.Factory[], Integer> factoriesAndCount) {
		final int index = INDEXES_BY_ID.getOrDefault(id, -1);
		if (index < 0) {
			throw new IllegalArgumentException("no pool with id: " + id);
		} else {
			mutateOffers(offers -> offers.set(index, factoriesAndCount));
		}
	}

	public static void registerPool(Identifier id, Pair<TradeOffers.Factory[], Integer> factoriesAndCount) {
		if (INDEXES_BY_ID.getOrDefault(id, -1) < 0) {
			throw new IllegalArgumentException("pool id %s is already registered".formatted(id));
		} else {
			mutateOffers(offers -> {
				INDEXES_BY_ID.put(id, offers.size());
				offers.add(factoriesAndCount);
			});
		}
	}

	private static void mutateOffers(Consumer<List<Pair<TradeOffers.Factory[], Integer>>> mutator) {
		final List<Pair<TradeOffers.Factory[], Integer>> mutableOffers = new ArrayList<>(
				TradeOffers.WANDERING_TRADER_TRADES
		);

		mutator.accept(mutableOffers);

		TradeOffersAccessor.quilt$setWANDERING_TRADER_TRADES(mutableOffers);
	}
}
