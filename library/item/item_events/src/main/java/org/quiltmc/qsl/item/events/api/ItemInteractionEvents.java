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

package org.quiltmc.qsl.item.events.api;

import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

public final class ItemInteractionEvents {
	private ItemInteractionEvents() {
		throw new UnsupportedOperationException("ItemInteractionEvents only contains static declarations.");
	}

	public static final Event<Used> USED = Event.create(Used.class, callbacks -> (stack, world, user, hand) -> {
		var result = TypedActionResult.pass(stack);
		for (var callback : callbacks) {
			result = callback.onItemUsed(stack, world, user, hand);
			if (result.getResult() != ActionResult.PASS) {
				return result;
			}
		}
		return result;
	});

	public static final Event<UsedOnBlock> USED_ON_BLOCK = Event.create(UsedOnBlock.class, callbacks -> context -> {
		var result = ActionResult.PASS;
		for (var callback : callbacks) {
			result = callback.onItemUsedOnBlock(context);
			if (result != ActionResult.PASS) {
				return result;
			}
		}
		return result;
	});

	public static final Event<UsedOnEntity> USED_ON_ENTITY = Event.create(UsedOnEntity.class, callbacks -> (stack, world, user, entity, hand) -> {
		var result = ActionResult.PASS;
		for (var callback : callbacks) {
			result = callback.onItemUsedOnEntity(stack, world, user, entity, hand);
			if (result != ActionResult.PASS) {
				return result;
			}
		}
		return result;
	});

	public static final Event<FinishedUsing> FINISHED_USING = Event.create(FinishedUsing.class, callbacks -> (stack, world, user) -> {
		for (var callback : callbacks) {
			stack = callback.onFinishedUsing(stack, world, user);
		}
		return stack;
	});

	@FunctionalInterface
	public interface Used extends EventAwareListener {
		@NotNull TypedActionResult<ItemStack> onItemUsed(
				@NotNull ItemStack stack, @NotNull World world, @NotNull PlayerEntity user, @NotNull Hand hand
		);
	}

	@FunctionalInterface
	public interface UsedOnBlock extends EventAwareListener {
		@NotNull ActionResult onItemUsedOnBlock(@NotNull ItemUsageContext context);
	}

	@FunctionalInterface
	public interface UsedOnEntity extends EventAwareListener {
		@NotNull ActionResult onItemUsedOnEntity(
				@NotNull ItemStack stack, @NotNull World world, @NotNull PlayerEntity user, @NotNull LivingEntity entity, @NotNull Hand hand
		);
	}

	@FunctionalInterface
	public interface FinishedUsing extends EventAwareListener {
		@NotNull ItemStack onFinishedUsing(
				@NotNull ItemStack stack, @NotNull World world, @NotNull LivingEntity user
		);
	}
}
