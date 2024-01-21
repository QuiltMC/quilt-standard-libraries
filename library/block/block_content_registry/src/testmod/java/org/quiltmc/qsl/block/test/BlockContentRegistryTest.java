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

package org.quiltmc.qsl.block.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.OxidizableBlock;
import net.minecraft.block.PillarBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.test.GameTest;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.content.registry.api.BlockContentRegistries;
import org.quiltmc.qsl.block.content.registry.api.FlammableBlockEntry;
import org.quiltmc.qsl.block.content.registry.api.ReversibleBlockEntry;
import org.quiltmc.qsl.block.content.registry.api.enchanting.ConstantBooster;
import org.quiltmc.qsl.block.content.registry.api.enchanting.EnchantingBooster;
import org.quiltmc.qsl.block.content.registry.api.enchanting.EnchantingBoosterType;
import org.quiltmc.qsl.block.content.registry.api.enchanting.EnchantingBoosters;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;
import org.quiltmc.qsl.registry.attachment.api.RegistryExtensions;
import org.quiltmc.qsl.testing.api.game.QuiltGameTest;
import org.quiltmc.qsl.testing.api.game.QuiltTestContext;

public class BlockContentRegistryTest implements ModInitializer, QuiltGameTest {
	public static final String MOD_ID = "quilt_block_content_registry_testmod";
	public static final Logger LOGGER = LoggerFactory.getLogger("BlockContentRegistryTest");

	public static boolean testPassed = false;

	@Override
	public void onInitialize(ModContainer mod) {
		RegistryExtensions.register(Registries.BLOCK, new Identifier(MOD_ID, "oxidizable_iron_block"),
				new OxidizableBlock(Oxidizable.OxidizationLevel.UNAFFECTED, AbstractBlock.Settings.method_9630(Blocks.IRON_BLOCK)),
				BlockContentRegistries.OXIDIZABLE, new ReversibleBlockEntry(Blocks.IRON_BLOCK, false));

		BlockContentRegistries.ENCHANTING_BOOSTERS.put(Blocks.IRON_BLOCK, new ConstantBooster(3f));
		BlockContentRegistries.ENCHANTING_BOOSTERS.put(Blocks.DIAMOND_BLOCK, new ConstantBooster(15f));
		BlockContentRegistries.ENCHANTING_BOOSTERS.put(Blocks.NETHERITE_BLOCK, new ConstantBooster(100f));
		BlockContentRegistries.ENCHANTING_BOOSTERS.put(Blocks.OAK_PLANKS, new ConstantBooster(0.25f));
		BlockContentRegistries.ENCHANTING_BOOSTERS.put(Blocks.REDSTONE_WIRE, new EnchantingBlockStateBooster());

		ServerWorldTickEvents.START.register((server, world) -> {
			if (testPassed) {
				return;
			}

			LOGGER.info("Starting BlockContentRegistry tests");
			Registries.BLOCK.getOrCreateTag(BlockTags.ANVILS).forEach(holder -> this.assertValues(holder.value(), BlockContentRegistries.FLAMMABLE, new FlammableBlockEntry(100, 100)));

			this.assertValues(Blocks.OAK_PLANKS, BlockContentRegistries.FLATTENABLE, Blocks.OAK_SLAB.getDefaultState());
			this.assertValues(Blocks.QUARTZ_PILLAR, BlockContentRegistries.STRIPPABLE, Blocks.PURPUR_PILLAR);
			this.assertValues(Blocks.IRON_BLOCK, BlockContentRegistries.WAXABLE, new ReversibleBlockEntry(Blocks.GOLD_BLOCK, true));
			LOGGER.info("Finished BlockContentRegistry tests");

			testPassed = true;
		});
	}

	@GameTest(structureName = QuiltGameTest.EMPTY_STRUCTURE)
	public void flatten(QuiltTestContext context) {
		var tester = new TestHelper(new BlockPos(1, 1, 1), new ItemStack(Items.IRON_SHOVEL));

		tester.push(Blocks.DIRT.getDefaultState(), Blocks.DIRT_PATH.getDefaultState());
		tester.push(Blocks.GRASS_BLOCK.getDefaultState(), Blocks.DIRT_PATH.getDefaultState());
		tester.push(Blocks.OAK_PLANKS.getDefaultState(), Blocks.OAK_SLAB.getDefaultState());

		tester.run(context);
	}

	@GameTest(structureName = QuiltGameTest.EMPTY_STRUCTURE)
	public void strip(QuiltTestContext context) {
		var tester = new TestHelper(new BlockPos(1, 1, 1), new ItemStack(Items.IRON_AXE));

		tester.push(Blocks.OAK_LOG.getDefaultState(), Blocks.STRIPPED_OAK_LOG.getDefaultState());
		tester.push(
				Blocks.OAK_LOG.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.Z),
				Blocks.STRIPPED_OAK_LOG.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.Z)
		);
		tester.push(Blocks.QUARTZ_PILLAR.getDefaultState(), Blocks.PURPUR_PILLAR.getDefaultState());
		tester.push(
				Blocks.QUARTZ_PILLAR.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.Z),
				Blocks.PURPUR_PILLAR.getDefaultState().with(PillarBlock.AXIS, Direction.Axis.Z)
		);

		tester.run(context);
	}

	private record EnchantingBlockStateBooster() implements EnchantingBooster {
		public static EnchantingBoosterType TYPE = EnchantingBoosters.register(new Identifier(MOD_ID, "block_state_booster"),
				new EnchantingBoosterType(Codec.unit(EnchantingBlockStateBooster::new), Optional.of(new EnchantingBlockStateBooster())));

		@Override
		public float getEnchantingBoost(World world, BlockState state, BlockPos pos) {
			if (!state.contains(Properties.POWER)) {
				return 0;
			}

			return state.get(Properties.POWER) / 15f;
		}

		@Override
		public EnchantingBoosterType getType() {
			return TYPE;
		}
	}

	private <T> void assertValues(Block block, RegistryEntryAttachment<Block, T> attachment, T value) {
		Optional<T> entry = attachment.get(block);
		Identifier id = Registries.BLOCK.getId(block);
		if (entry.isEmpty()) {
			throw new AssertionError("No entry present for " + id);
		}

		if (!entry.get().equals(value)) {
			throw new AssertionError("Value incorrect for " + id);
		}

		LOGGER.info("Test for block " + id + " passed for REA " + attachment.id());
	}

	static class TestHelper {
		private final List<Entry> entries = new ArrayList<>();
		private final ItemStack tool;
		private BlockPos nextPos;

		TestHelper(BlockPos startPos, ItemStack tool) {
			this.tool = tool;
			this.nextPos = startPos;
		}

		void push(BlockState baseState, BlockState targetState) {
			this.entries.add(new Entry(this.nextPos, baseState, targetState));

			this.nextPos = this.nextPos.east();
			if (this.nextPos.getX() > 7) {
				this.nextPos = new BlockPos(1, 1, this.nextPos.getZ() + 1);
			}
		}

		void run(QuiltTestContext context) {
			this.entries.forEach(entry -> context.setBlockState(entry.pos(), entry.baseState()));

			var player = context.createMockPlayer();
			this.entries.forEach(entry -> {
				context.useStackOnBlockAt(player, this.tool, entry.pos(), Direction.UP);
			});

			context.succeedWhen(() ->
					this.entries.forEach(entry ->
							context.checkBlockState(entry.pos(), state -> state.equals(entry.targetState()),
									() -> "Could not find state " + entry.targetState()
							)
					)
			);
		}

		record Entry(BlockPos pos, BlockState baseState, BlockState targetState) {}
	}
}
