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

package org.quiltmc.qsl.entity.effect.test;

import com.mojang.serialization.MapCodec;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectType;
import net.minecraft.item.ConsumeEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAnimation;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.entity.effect.api.StatusEffectEvents;
import org.quiltmc.qsl.entity.effect.api.StatusEffectRemovalReason;

public final class StatusEffectTest implements ModInitializer {
	public static final String NAMESPACE = "quilt_status_effect_testmod";

	public static Identifier createId(String path) {
		return Identifier.of(NAMESPACE, path);
	}

	public static class DrankPasteurizedMilkRemovalReason extends StatusEffectRemovalReason.ConsumeRemovalReason {
		public static final Identifier DRANK_PASTEURIZED_MILK_ID = createId("action.consume.drank_pasteurized_milk");
		public DrankPasteurizedMilkRemovalReason(ItemStack stack) {
			super(DRANK_PASTEURIZED_MILK_ID, stack);
		}

		@Override
		public boolean removesEffect(StatusEffectInstance effect) {
			return effect.getEffectType().getValue().getType() == StatusEffectType.HARMFUL;
		}
	}

	public record DrankPasteurizedMilk() implements ConsumeEffect {
		public static final ConsumeEffect.Type<DrankPasteurizedMilk> DRANK_PASTEURIZED_MILK = Registry.register(
				Registries.CONSUME_EFFECT_TYPE,
				createId("drank_pasteurized_milk"),
				new ConsumeEffect.Type<>(DrankPasteurizedMilk.CODEC, DrankPasteurizedMilk.PACKET_CODEC)
		);

		public static final DrankPasteurizedMilk INSTANCE = new DrankPasteurizedMilk();
		public static final MapCodec<DrankPasteurizedMilk> CODEC = MapCodec.unit(INSTANCE);
		public static final PacketCodec<RegistryByteBuf, DrankPasteurizedMilk> PACKET_CODEC = PacketCodec.unit(INSTANCE);

		@Override
		public Type<? extends ConsumeEffect> getType() {
			return DRANK_PASTEURIZED_MILK;
		}

		@Override
		public boolean apply(World world, ItemStack stack, LivingEntity entity) {
			return entity.clearStatusEffects(new DrankPasteurizedMilkRemovalReason(stack)) > 0;
		}
	}

	@Override
	public void onInitialize(ModContainer mod) {
		RegistryKey<Item> bucketKey = RegistryKey.of(RegistryKeys.ITEM, createId("pasteurized_milk_bucket"));
		Registry.register(Registries.ITEM, bucketKey, new Item(
			new Item.Settings()
				.key(bucketKey)
				.recipeRemainder(Items.BUCKET)
				.useRemainder(Items.BUCKET)
				.maxCount(1)
				.component(DataComponentTypes.CONSUMABLE,
					ConsumableComponent.builder()
						.consumeSeconds(1.6F)
						.animation(UseAnimation.DRINK)
						.sound(SoundEvents.ENTITY_GENERIC_DRINK)
						.hasConsumeParticles(false)
						.effect(DrankPasteurizedMilk.INSTANCE)
						.build())
		));

		StatusEffectEvents.ON_REMOVED.register((entity, effect, reason) -> {
			if (reason instanceof DrankPasteurizedMilkRemovalReason milk) {
				System.out.println("Removed " + effect.getTranslationKey() + " with pasteurized milk{" + milk.stack().toString() + "}!");
			} else if (reason instanceof StatusEffectRemovalReason.ConsumeRemovalReason consume) {
				System.out.println("Removed " + effect.getTranslationKey() + " with consume reason " + consume.getId() + "{" + consume.stack().toString() + "}!");
			} else {
				System.out.println("Removed " + effect.getTranslationKey() + " with reason " + reason.getId());
			}
		});
	}
}
