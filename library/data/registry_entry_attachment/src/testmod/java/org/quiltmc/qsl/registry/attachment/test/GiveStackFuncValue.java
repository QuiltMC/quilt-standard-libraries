/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.registry.attachment.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class GiveStackFuncValue extends FuncValue {
	public static final Identifier TYPE = new Identifier("quilt", "give_stack");
	public static final Codec<GiveStackFuncValue> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(ItemStack.CODEC.fieldOf("stack").forGetter(gs -> gs.stack))
					.apply(instance, GiveStackFuncValue::new));

	private final ItemStack stack;

	public GiveStackFuncValue(ItemStack stack) {
		super(TYPE);
		this.stack = stack;
	}

	@Override
	public void invoke(ServerPlayerEntity player) {
		player.getInventory().offerOrDrop(this.stack.copy());
	}

	@Override
	public String toString() {
		return "give_stack{" + Registries.ITEM.getId(this.stack.getItem()) + " x" + this.stack.getCount() + "}";
	}
}
