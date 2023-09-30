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

package org.quiltmc.qsl.villager.test;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.VillagerProfession;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.villager.api.TradeOfferHelper;

/*
 * Second entrypoint to validate class loading does not break this.
 */
public class VillagerTypeTest2 implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 1, factories -> {
			factories.add(new SimpleTradeFactory(new TradeOffer(new ItemStack(Items.DIAMOND, 5), new ItemStack(Items.NETHERITE_INGOT), 3, 4, 0.15F)));
		});
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 1, factories -> {
			factories.add(new SimpleTradeFactory(new TradeOffer(new ItemStack(Items.DIAMOND, 6), new ItemStack(Items.ELYTRA), 3, 4, 0.15F)));
		});
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 1, factories -> {
			factories.add(new SimpleTradeFactory(new TradeOffer(new ItemStack(Items.DIAMOND, 7), new ItemStack(Items.CHAINMAIL_BOOTS), 3, 4, 0.15F)));
		});
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 1, factories -> {
			factories.add(new SimpleTradeFactory(new TradeOffer(new ItemStack(Items.DIAMOND, 8), new ItemStack(Items.CHAINMAIL_CHESTPLATE), 3, 4, 0.15F)));
		});
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 1, factories -> {
			factories.add(new SimpleTradeFactory(new TradeOffer(new ItemStack(Items.DIAMOND, 9), new ItemStack(Items.CHAINMAIL_HELMET), 3, 4, 0.15F)));
		});
		TradeOfferHelper.registerVillagerOffers(VillagerProfession.ARMORER, 1, factories -> {
			factories.add(new SimpleTradeFactory(new TradeOffer(new ItemStack(Items.DIAMOND, 10), new ItemStack(Items.CHAINMAIL_LEGGINGS), 3, 4, 0.15F)));
		});
	}
}
