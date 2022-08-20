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

package org.quiltmc.qsl.block.enchantmentstuff.api;

import com.mojang.serialization.Codec;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;

public class EnchantmentPowerAttachment {
	/**
	 * A {@link RegistryEntryAttachment} for enchantment power levels in bookshelf equivalents.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt_block_enchantment_stuff/attachments/minecraft/block/power.json}
	 */
	public static final RegistryEntryAttachment<Block, Float> POWER_LEVEL = RegistryEntryAttachment
			.builder(Registry.BLOCK,
					new Identifier("quilt_block_enchantment_stuff", "power"),
					Float.class,
					Codec.floatRange(0f, Float.MAX_VALUE))
			.build();
}
