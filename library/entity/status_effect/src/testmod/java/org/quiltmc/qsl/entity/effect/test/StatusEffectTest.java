/*
 * Copyright 2022-2023 QuiltMC
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

package org.quiltmc.qsl.entity.effect.test;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.entity.effect.api.StatusEffectRemovalReason;

public final class StatusEffectTest implements ModInitializer {
	public static final String NAMESPACE = "quilt_status_effect_testmod";

	public static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}

	public static final StatusEffectRemovalReason DRANK_PASTEURIZED_MILK = new StatusEffectRemovalReason(id("action.drank_pasteurized_milk")) {
		@Override
		public boolean removesEffect(StatusEffectInstance effect) {
			return effect.getEffectType().getType() == StatusEffectType.HARMFUL;
		}
	};

	public static final Item PASTEURIZED_MILK_BUCKET = Registry.register(Registries.ITEM, id("pasteurized_milk_bucket"),
			new PasteurizedMilkBucketItem(new Item.Settings()
					.recipeRemainder(Items.BUCKET)
					.maxCount(1)));

	@Override
	public void onInitialize(ModContainer mod) {}
}
