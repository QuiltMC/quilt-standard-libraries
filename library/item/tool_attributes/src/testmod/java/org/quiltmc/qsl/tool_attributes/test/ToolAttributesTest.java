/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.tool_attributes.test;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.tag.Tag;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.block.extensions.api.QuiltMaterialBuilder;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.tag.api.TagRegistry;
import org.quiltmc.qsl.tool_attributes.api.DynamicAttributeTool;
import org.quiltmc.qsl.tool_attributes.api.QuiltToolTags;
import org.quiltmc.qsl.tool_attributes.test.item.TestDynamicCancelItem;
import org.quiltmc.qsl.tool_attributes.test.item.TestDynamicSwordItem;
import org.quiltmc.qsl.tool_attributes.test.item.TestDynamicToolItem;
import org.quiltmc.qsl.tool_attributes.test.item.TestNullableItem;

import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;

public class ToolAttributesTest implements ModInitializer {
	private static final float DEFAULT_BREAK_SPEED = 1f;
	private static final float TOOL_BREAK_SPEED = 10f;

	// A custom tool type, pineapples
	private static final Tag<Item> PINEAPPLE = TagRegistry.ITEM.create(new Identifier("quilt_tool_attributes_testmod", "pineapples"));

	private boolean hasValidated = false;

	Block gravelBlock;
	Block stoneBlock;
	Item testShovel;
	Item testPickaxe;
	Item testSword;

	Item testStoneLevelTater;
	Item testStoneDynamicLevelTater;
	Item testDiamondLevelTater;
	Item testDiamondDynamicLevelTater;
	Block pineappleEffectiveBlock;

	// Simple blocks that only need a tool without a specific mining level (legacy technique using block settings)
	Block needsShears;
	Block needsSword;
	Block needsPickaxe;
	Block needsAxe;
	Block needsHoe;
	Block needsShovel;

	// These items are only tagged, but are not actual ToolItems or DynamicAttributeTools.
	Item fakeShears;
	Item fakeSword;
	Item fakePickaxe;
	Item fakeAxe;
	Item fakeHoe;
	Item fakeShovel;

	@Override
	public void onInitialize() {
		init();
	}

	public void init() {
		// Register a custom shovel that has a mining level of 2 (iron) dynamically.
		testShovel = Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "test_shovel"), new TestTool(new Item.Settings(), QuiltToolTags.SHOVELS, 2));
		//Register a custom pickaxe that has a mining level of 2 (iron) dynamically.
		testPickaxe = Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "test_pickaxe"), new TestTool(new Item.Settings(), QuiltToolTags.PICKAXES, 2));
		//Register a custom sword that has a mining level of 2 (iron) dynamically.
		testSword = Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "test_sword"), new TestTool(new Item.Settings(), QuiltToolTags.SWORDS, 2));
		// Register a block that requires a shovel that is as strong or stronger than an iron one.
		gravelBlock = Registry.register(Registry.BLOCK, new Identifier("quilt_tool_attributes_testmod", "hardened_gravel_block"),
				new Block(QuiltBlockSettings.of(new QuiltMaterialBuilder(MapColor.PALE_YELLOW).build(), MapColor.STONE_GRAY)
						.requiresTool()
						.strength(0.6F)
						.sounds(BlockSoundGroup.GRAVEL)));
		Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "hardened_gravel_block"), new BlockItem(gravelBlock, new Item.Settings()));
		// Register a block that requires a pickaxe that is as strong or stronger than an iron one.
		stoneBlock = Registry.register(Registry.BLOCK, new Identifier("quilt_tool_attributes_testmod", "hardened_stone_block"),
				new Block(QuiltBlockSettings.of(Material.STONE, MapColor.STONE_GRAY)
						.requiresTool()
						.strength(0.6F)
						.sounds(BlockSoundGroup.STONE)));
		Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "hardened_stone_block"), new BlockItem(stoneBlock, new Item.Settings()));

		// Register a pineapple that has a mining level of 1 (stone).
		testStoneLevelTater = Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "test_stone_level_pineapple"), new ToolItem(ToolMaterials.STONE, new Item.Settings()));
		// Register a pineapple that has a mining level of 1 (stone) dynamically.
		testStoneDynamicLevelTater = Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "test_stone_dynamic_level_pineapple"), new TestTool(new Item.Settings(), PINEAPPLE, 1));
		//Register a pineapple that has a mining level of 3 (diamond).
		testDiamondLevelTater = Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "test_diamond_level_pineapple"), new ToolItem(ToolMaterials.DIAMOND, new Item.Settings()));
		//Register a pineapple that has a mining level of 3 (diamond) dynamically.
		testDiamondDynamicLevelTater = Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "test_diamond_dynamic_level_pineapple"), new TestTool(new Item.Settings(), PINEAPPLE, 3));

		pineappleEffectiveBlock = Registry.register(Registry.BLOCK, new Identifier("quilt_tool_attributes_testmod", "pineapple_effective_block"),
				new Block(QuiltBlockSettings.of(Material.ORGANIC_PRODUCT, MapColor.ORANGE)
						.requiresTool()
						.strength(0.6F)
						.sounds(BlockSoundGroup.CROP)));
		Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "pineapple_effective_block"), new BlockItem(pineappleEffectiveBlock, new Item.Settings()));

		// DYNAMIC ATTRIBUTE MODIFIERS
		// The Dynamic Sword tests to make sure standard vanilla attributes can co-exist with dynamic attributes.
		Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "dynamic_sword"), new TestDynamicSwordItem(new Item.Settings()));
		// The Dynamic Tool ensures a tool can have dynamic attributes (with no vanilla atributes). It applies 2 layers of speed reduction to the player.
		Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "dynamic_tool"), new TestDynamicToolItem(new Item.Settings()));
		// Test cancels-out attributes
		Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "cancel_item"), new TestDynamicCancelItem(new Item.Settings()));
		// Test parameter nullability
		Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "null_test"), new TestNullableItem(new Item.Settings()));

		needsShears = Registry.register(Registry.BLOCK, new Identifier("quilt_tool_attributes_testmod", "needs_shears"), new Block(QuiltBlockSettings.of(Material.STONE).requiresTool().strength(1, 1)));
		needsSword = Registry.register(Registry.BLOCK, new Identifier("quilt_tool_attributes_testmod", "needs_sword"), new Block(QuiltBlockSettings.of(Material.STONE).requiresTool().strength(1, 1)));
		needsPickaxe = Registry.register(Registry.BLOCK, new Identifier("quilt_tool_attributes_testmod", "needs_pickaxe"), new Block(QuiltBlockSettings.of(Material.STONE).requiresTool().strength(1, 1)));
		needsAxe = Registry.register(Registry.BLOCK, new Identifier("quilt_tool_attributes_testmod", "needs_axe"), new Block(QuiltBlockSettings.of(Material.STONE).requiresTool().strength(1, 1)));
		needsHoe = Registry.register(Registry.BLOCK, new Identifier("quilt_tool_attributes_testmod", "needs_hoe"), new Block(QuiltBlockSettings.of(Material.STONE).requiresTool().strength(1, 1)));
		needsShovel = Registry.register(Registry.BLOCK, new Identifier("quilt_tool_attributes_testmod", "needs_shovel"), new Block(QuiltBlockSettings.of(Material.STONE).requiresTool().strength(1, 1)));

		// "Fake" tools, see explanation above
		fakeShears = Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "fake_shears"), new Item(new Item.Settings()));
		fakeSword = Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "fake_sword"), new Item(new Item.Settings()));
		fakePickaxe = Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "fake_pickaxe"), new Item(new Item.Settings()));
		fakeAxe = Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "fake_axe"), new Item(new Item.Settings()));
		fakeHoe = Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "fake_hoe"), new Item(new Item.Settings()));
		fakeShovel = Registry.register(Registry.ITEM, new Identifier("quilt_tool_attributes_testmod", "fake_shovel"), new Item(new Item.Settings()));

//		ServerTickEvents.START.register(this::validate);
		CommandRegistrationCallback.EVENT.register((dispatcher, integrated, dedicated) -> {
			dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("test_tool_attributes").executes(context -> {
				validate(context);
				return 1;
			}));
		});
	}

	private void validate(CommandContext<ServerCommandSource> context) {
		if (QuiltToolTags.PICKAXES.values().isEmpty()) {
			throw new AssertionError("Failed to load tool tags");
		}

		//Test we haven't broken vanilla behavior
		context.getSource().sendFeedback(new LiteralText("Vanilla behavior"), false);
		testToolOnBlock(new ItemStack(Items.STONE_PICKAXE), Blocks.GRAVEL, false, 1.0F, context);
		testToolOnBlock(new ItemStack(Items.IRON_PICKAXE), Blocks.STONE, true, ((ToolItem) Items.IRON_PICKAXE).getMaterial().getMiningSpeedMultiplier(), context);
		testToolOnBlock(new ItemStack(Items.IRON_PICKAXE), Blocks.OBSIDIAN, false, ((ToolItem) Items.IRON_PICKAXE).getMaterial().getMiningSpeedMultiplier(), context);
		testToolOnBlock(new ItemStack(Items.STONE_SHOVEL), Blocks.STONE, false, 1.0F, context);
		testToolOnBlock(new ItemStack(Items.STONE_SHOVEL), Blocks.GRAVEL, true, ((ToolItem) Items.STONE_SHOVEL).getMaterial().getMiningSpeedMultiplier(), context);

		//Test vanilla tools don't bypass fabric mining levels
		context.getSource().sendFeedback(new LiteralText("Check vanilla doesnt bypass mining level api"), false);
		testToolOnBlock(new ItemStack(Items.STONE_PICKAXE), stoneBlock, false, ((ToolItem) Items.STONE_PICKAXE).getMaterial().getMiningSpeedMultiplier(), context);
		testToolOnBlock(new ItemStack(Items.IRON_PICKAXE), stoneBlock, true, ((ToolItem) Items.IRON_PICKAXE).getMaterial().getMiningSpeedMultiplier(), context);
		testToolOnBlock(new ItemStack(Items.STONE_SHOVEL), gravelBlock, false, ((ToolItem) Items.STONE_SHOVEL).getMaterial().getMiningSpeedMultiplier(), context);
		testToolOnBlock(new ItemStack(Items.IRON_SHOVEL), gravelBlock, true, ((ToolItem) Items.IRON_SHOVEL).getMaterial().getMiningSpeedMultiplier(), context);

		//Test vanilla tools respect fabric mining tags
		context.getSource().sendFeedback(new LiteralText("Check vanilla respects mining level tags"), false);
		testToolOnBlock(new ItemStack(Items.IRON_PICKAXE), gravelBlock, false, DEFAULT_BREAK_SPEED, context);
		testToolOnBlock(new ItemStack(Items.IRON_SHOVEL), stoneBlock, false, DEFAULT_BREAK_SPEED, context);

		//Test dynamic tools don't bypass mining level
		context.getSource().sendFeedback(new LiteralText("Check dynamic respects mining level api"), false);
		testToolOnBlock(new ItemStack(testPickaxe), Blocks.OBSIDIAN, false, TOOL_BREAK_SPEED, context);

		//Test dynamic tools respect fabric mining tags
		context.getSource().sendFeedback(new LiteralText("Check dynamic respects mining level tags"), false);
		testToolOnBlock(new ItemStack(testPickaxe), gravelBlock, false, DEFAULT_BREAK_SPEED, context);
		testToolOnBlock(new ItemStack(testShovel), stoneBlock, false, DEFAULT_BREAK_SPEED, context);

		//Test dynamic tools on vanilla blocks
		context.getSource().sendFeedback(new LiteralText("Check vanilla on vanilla blocks"), false);
		testToolOnBlock(new ItemStack(testShovel), Blocks.STONE, false, DEFAULT_BREAK_SPEED, context);
		testToolOnBlock(new ItemStack(testShovel), Blocks.GRAVEL, true, TOOL_BREAK_SPEED, context);
		testToolOnBlock(new ItemStack(testPickaxe), Blocks.GRAVEL, false, DEFAULT_BREAK_SPEED, context);
		testToolOnBlock(new ItemStack(testPickaxe), Blocks.STONE, true, TOOL_BREAK_SPEED, context);

		//Test pineapples respect our pineapple block
		context.getSource().sendFeedback(new LiteralText("Check pineapples respects pineapple blocks"), false);
		testToolOnBlock(new ItemStack(testDiamondDynamicLevelTater), pineappleEffectiveBlock, true, TOOL_BREAK_SPEED, context);
		testToolOnBlock(new ItemStack(testDiamondLevelTater), pineappleEffectiveBlock, true, ToolMaterials.DIAMOND.getMiningSpeedMultiplier(), context);
		testToolOnBlock(new ItemStack(testStoneDynamicLevelTater), pineappleEffectiveBlock, false, TOOL_BREAK_SPEED, context);
		testToolOnBlock(new ItemStack(testStoneLevelTater), pineappleEffectiveBlock, false, ToolMaterials.STONE.getMiningSpeedMultiplier(), context);

		//Test other tools on our pineapple block
		context.getSource().sendFeedback(new LiteralText("Check tools respects pineapple blocks"), false);
		testToolOnBlock(new ItemStack(testPickaxe), pineappleEffectiveBlock, false, DEFAULT_BREAK_SPEED, context);
		testToolOnBlock(new ItemStack(testShovel), pineappleEffectiveBlock, false, DEFAULT_BREAK_SPEED, context);
		testToolOnBlock(new ItemStack(Items.IRON_PICKAXE), pineappleEffectiveBlock, false, DEFAULT_BREAK_SPEED, context);
		testToolOnBlock(new ItemStack(Items.IRON_SHOVEL), pineappleEffectiveBlock, false, DEFAULT_BREAK_SPEED, context);

		//Test vanilla tools on blocks
		context.getSource().sendFeedback(new LiteralText("Check vanilla on blocks"), false);
		testToolOnBlock(new ItemStack(Items.SHEARS), needsShears, true, DEFAULT_BREAK_SPEED, context);
		testToolOnBlock(new ItemStack(Items.IRON_SWORD), needsSword, true, ToolMaterials.IRON.getMiningSpeedMultiplier(), context);
		testToolOnBlock(new ItemStack(Items.IRON_AXE), needsAxe, true, ToolMaterials.IRON.getMiningSpeedMultiplier(), context);
		testToolOnBlock(new ItemStack(Items.IRON_PICKAXE), needsPickaxe, true, ToolMaterials.IRON.getMiningSpeedMultiplier(), context);
		testToolOnBlock(new ItemStack(Items.IRON_HOE), needsHoe, true, ToolMaterials.IRON.getMiningSpeedMultiplier(), context);
		testToolOnBlock(new ItemStack(Items.IRON_SHOVEL), needsShovel, true, ToolMaterials.IRON.getMiningSpeedMultiplier(), context);

		//Test fake tools on corresponding and invalid blocks
		context.getSource().sendFeedback(new LiteralText("Check fake on real and fake blocks"), false);
		// Note: using LinkedHashMultimap to ensure the same order (this makes it more predictable when debugging)
		Multimap<Item, Block> fakeToolsToEffectiveBlocks = LinkedHashMultimap.create(6, 2);
		fakeToolsToEffectiveBlocks.put(fakeShears, needsShears);
		fakeToolsToEffectiveBlocks.put(fakeSword, needsSword);
		fakeToolsToEffectiveBlocks.put(fakeAxe, needsAxe);
		fakeToolsToEffectiveBlocks.put(fakePickaxe, needsPickaxe);
		fakeToolsToEffectiveBlocks.put(fakeHoe, needsHoe);
		fakeToolsToEffectiveBlocks.put(fakeShovel, needsShovel);
		testExclusivelyEffective(fakeToolsToEffectiveBlocks, (tool, block) -> {
			return DEFAULT_BREAK_SPEED;
		}, context);

		//Test fake tools on corresponding and invalid blocks
		Multimap<Item, Block> dynamicToolsToEffectiveBlocks = LinkedHashMultimap.create(3, 2);
		dynamicToolsToEffectiveBlocks.put(testSword, needsSword);
		dynamicToolsToEffectiveBlocks.put(testPickaxe, needsPickaxe);
		dynamicToolsToEffectiveBlocks.put(testShovel, needsShovel);
		testExclusivelyEffective(dynamicToolsToEffectiveBlocks, (tool, block) -> TOOL_BREAK_SPEED, context);
	}

	private void testExclusivelyEffective(Multimap<Item, Block> itemsToEffectiveBlocks, BiFunction<Item, Block, Float> effectiveSpeed, CommandContext<ServerCommandSource> context) {
		for (List<ItemConvertible> pair : Sets.cartesianProduct(itemsToEffectiveBlocks.keySet(), new HashSet<>(itemsToEffectiveBlocks.values()))) {
			Item item = (Item) pair.get(0);
			Block block = (Block) pair.get(1);

			if (itemsToEffectiveBlocks.get(item).contains(block)) {
				testToolOnBlock(new ItemStack(item), block, true, effectiveSpeed.apply(item, block), context);
			} else {
				testToolOnBlock(new ItemStack(item), block, false, DEFAULT_BREAK_SPEED, context);
			}
		}
	}

	public static final Style FAIL = Style.EMPTY.withColor(TextColor.parse("red"));
	public static final Style SUCCESS = Style.EMPTY.withColor(TextColor.parse("green"));

	private void testToolOnBlock(ItemStack item, Block block, boolean inEffective, float inSpeed, CommandContext<ServerCommandSource> context) {
		boolean effective = item.isSuitableFor(block.getDefaultState());
		float speed = item.getMiningSpeedMultiplier(block.getDefaultState());

		context.getSource().sendFeedback(new LiteralText("\tCheck for " + Registry.ITEM.getId(item.getItem()) + " breaking " + Registry.BLOCK.getId(block) + ":"), false);

		boolean failed = false;
		if (inEffective != effective) {
			context.getSource().sendFeedback(new LiteralText("\t\tEffective check incorrect. Found " + effective + ", expected " + inEffective).setStyle(FAIL), true);
			failed = true;
		}
		if (inSpeed != speed) {
			context.getSource().sendFeedback(new LiteralText("\t\tSpeed check incorrect. Found " + speed + ", expected " + inSpeed).setStyle(FAIL), true);
			failed = true;
		}

		if (!failed) {
			context.getSource().sendFeedback(new LiteralText("\t\tSuccess").setStyle(SUCCESS), true);
		}
	}

	private static class TestTool extends Item implements DynamicAttributeTool {
		final Tag<Item> toolType;
		final int miningLevel;

		public TestTool(Settings settings, Tag<Item> toolType, int miningLevel) {
			super(settings);
			this.toolType = toolType;
			this.miningLevel = miningLevel;
		}

		@Override
		public int getMiningLevel(Tag<Item> tag, BlockState state, ItemStack stack, LivingEntity user) {
			if (tag.equals(toolType)) return this.miningLevel;

			return 0;
		}

		@Override
		public float getMiningSpeedMultiplier(Tag<Item> tag, BlockState state, ItemStack stack, LivingEntity user) {
			if (tag.equals(toolType)) return TOOL_BREAK_SPEED;

			return DEFAULT_BREAK_SPEED;
		}
	}
}
