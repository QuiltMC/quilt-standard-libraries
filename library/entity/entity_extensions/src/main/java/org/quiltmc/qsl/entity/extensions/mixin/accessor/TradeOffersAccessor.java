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

package org.quiltmc.qsl.entity.extensions.mixin.accessor;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.village.TradeOffers;

import org.quiltmc.qsl.entity.extensions.impl.WanderingTraderOffersManager;

@Mixin(TradeOffers.class)
public interface TradeOffersAccessor {
	/**
	 * Only for use in {@link WanderingTraderOffersManager}.
	 */
	@Mutable
	@Accessor("WANDERING_TRADER_TRADES")
	static void quilt$setWANDERING_TRADER_TRADES(List<Pair<TradeOffers.Factory[], Integer>> trades) {
		throw new AssertionError("dummy method body reached");
	}
}
