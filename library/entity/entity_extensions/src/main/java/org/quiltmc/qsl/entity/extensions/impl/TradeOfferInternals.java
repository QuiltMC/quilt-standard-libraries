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

package org.quiltmc.qsl.entity.extensions.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

public final class TradeOfferInternals {
	private TradeOfferInternals() {
		throw new UnsupportedOperationException(
			TradeOfferInternals.class.getSimpleName() + " contains only static members"
		);
	}

	private static final Multimap<Identifier, TradeOffers.Factory[]> PENDING_WANDERING_TRADER_FACTORIES_BY_ID =
			HashMultimap.create();

	// synchronized guards against concurrent modifications - Vanilla does not mutate the underlying arrays (as of 1.16),
	// so reads will be fine without locking.
	public static synchronized void addToVillagerOfferPool(
			@NotNull
			RegistryKey<VillagerProfession> profession,
			int level,
			@NotNull
			TradeOffers.Factory[] factories
	) {
		addToVillagerOfferPoolImpl(
				profession, level, factories,
				TradeOffers.PROFESSION_TO_LEVELED_TRADE
		);
	}

	public static synchronized void addToExperimentalVillagerOfferPool(
			@NotNull
			RegistryKey<VillagerProfession> profession,
			int level,
			@NotNull
			TradeOffers.Factory[] factories
	) {
		addToVillagerOfferPoolImpl(
				profession, level, factories,
				TradeOffers.EXPERIMENTAL_TRADES
		);
	}

	private static synchronized void addToVillagerOfferPoolImpl(
			@NotNull
			RegistryKey<VillagerProfession> profession,
			int level,
			@NotNull
			TradeOffers.Factory[] factories,
			Map<RegistryKey<VillagerProfession>, Int2ObjectMap<TradeOffers.Factory[]>> tradesByProfession
	) {
		Objects.requireNonNull(profession, "profession must not be null");

		if (level < 0) {
			throw new IllegalArgumentException("level must not be negative; was:" + level);
		}

		validateFactories(factories);

		Int2ObjectMap<TradeOffers.Factory[]> leveledTradeMap = tradesByProfession
				.computeIfAbsent(profession, key -> new Int2ObjectOpenHashMap<>());

		TradeOffers.Factory[] oldFactories =
				leveledTradeMap.computeIfAbsent(level, key -> new TradeOffers.Factory[0]);

		leveledTradeMap.put(level, ArrayUtils.addAll(oldFactories, factories));
	}

	public static synchronized void addToWanderingTraderOfferPool(
			@NotNull
			Identifier id,
			@NotNull
			TradeOffers.Factory[] factories
	) {
		validateId(id);
		validateFactories(factories);

		WanderingTraderOffersManager.getPool(id).ifPresentOrElse(
				oldFactoriesAndCount -> {
					TradeOffers.Factory[] mergedFactories =
							ArrayUtils.addAll(oldFactoriesAndCount.getLeft(), factories);
					Pair<TradeOffers.Factory[], Integer> mergedFactoriesAndCount =
							Pair.of(mergedFactories, oldFactoriesAndCount.getRight());

					WanderingTraderOffersManager.setPool(id, mergedFactoriesAndCount);
				},
				() -> {
					PENDING_WANDERING_TRADER_FACTORIES_BY_ID.put(id, factories);
				}
		);
	}

	public static synchronized void registerWanderingTraderPool(
			@NotNull
			Identifier id,
			int count,
			@NotNull
			TradeOffers.Factory[] factories
	) {
		validateId(id);

		if (count <= 0) {
			throw new IllegalArgumentException("count must be greater than 0; was: " + count);
		}

		validateFactories(factories);

		Collection<TradeOffers.Factory[]> pendingFactories =
				PENDING_WANDERING_TRADER_FACTORIES_BY_ID.removeAll(id);

		TradeOffers.Factory[] mergedFactories;
		if (pendingFactories.isEmpty()) {
			mergedFactories = factories;
		} else {
			mergedFactories = Stream
				.concat(
					Arrays.stream(factories),
					pendingFactories.stream().flatMap(Arrays::stream)
				)
				.toArray(TradeOffers.Factory[]::new);
		}

		Pair<TradeOffers.Factory[], Integer> factoriesAndCount = Pair.of(mergedFactories, count);

		WanderingTraderOffersManager.registerPool(id, factoriesAndCount);
	}

	private static void validateId(@NotNull Identifier id) {
		Objects.requireNonNull(id, "id must not be null");
	}

	private static void validateFactories(TradeOffers.Factory[] factories) {
		if (Objects.requireNonNull(factories, "factories must not be null").length == 0) {
			throw new IllegalArgumentException("factories must not be empty");
		}
	}
}
