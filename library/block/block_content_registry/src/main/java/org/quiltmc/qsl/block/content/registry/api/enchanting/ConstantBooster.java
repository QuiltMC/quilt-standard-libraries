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

package org.quiltmc.qsl.block.content.registry.api.enchanting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.BlockState;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A constant enchanting booster.
 *
 * @param value the boost level
 */
public record ConstantBooster(float value) implements EnchantingBooster {
	public static final MapCodec<ConstantBooster> CODEC = Codec.FLOAT.fieldOf("value").xmap(ConstantBooster::new, ConstantBooster::value);
	public static final PacketCodec<RegistryByteBuf, ConstantBooster> PACKET_CODEC = PacketCodecs.FLOAT.map(ConstantBooster::new, ConstantBooster::value).cast();
	public static EnchantingBoosterType TYPE = EnchantingBoosters.register(Identifier.of("quilt", "constant"), CODEC, PACKET_CODEC);

	@Override
	public float getEnchantingBoost(World world, BlockState state, BlockPos pos) {
		return this.value;
	}

	@Override
	public EnchantingBoosterType getType() {
		return TYPE;
	}
}
