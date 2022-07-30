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

package org.quiltmc.qsl.block.test;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.content.registry.api.BlockContentRegistries;
import org.quiltmc.qsl.block.content.registry.api.ReversibleBlockEntry;
import org.quiltmc.qsl.registry.attachment.api.RegistryExtensions;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.OxidizableBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class BlockContentRegistryTest implements ModInitializer {
	public static final String MOD_ID = "quilt_block_content_registry_testmod";

	@Override
	public void onInitialize(ModContainer mod) {
		RegistryExtensions.register(Registry.BLOCK, new Identifier(MOD_ID, "oxidizable_iron_block"),
				new OxidizableBlock(Oxidizable.OxidizationLevel.UNAFFECTED, AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)),
				BlockContentRegistries.OXIDIZABLE_BLOCK, new ReversibleBlockEntry(Blocks.IRON_BLOCK, false));
	}
}
