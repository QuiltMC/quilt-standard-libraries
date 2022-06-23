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

package org.quiltmc.qsl.component.test;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.level.LevelProperties;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.api.components.FloatComponent;
import org.quiltmc.qsl.component.api.components.IntegerComponent;
import org.quiltmc.qsl.component.api.components.InventoryComponent;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;

import java.util.Objects;

public class ComponentTestMod implements ModInitializer {
	public static final String MODID = "quilt_component_test";

	// Registration Code
	public static final ComponentIdentifier<InventoryComponent> COW_INVENTORY = InventoryComponent.of(
			() -> DefaultedList.ofSize(1, new ItemStack(Items.COBBLESTONE, 64)),
			new Identifier(MODID, "cow_inventory")
	);
	public static final ComponentIdentifier<IntegerComponent> CREEPER_EXPLODE_TIME =
			IntegerComponent.create(200, new Identifier(MODID, "creeper_explode_time"));
	public static final ComponentIdentifier<IntegerComponent> HOSTILE_EXPLODE_TIME =
			IntegerComponent.create(new Identifier(MODID, "hostile_explode_time"));
	public static final ComponentIdentifier<IntegerComponent> CHEST_NUMBER =
			IntegerComponent.create(200, new Identifier(MODID, "chest_number"));
	public static final ComponentIdentifier<InventoryComponent> CHUNK_INVENTORY = InventoryComponent.ofSize(1,
			new Identifier(MODID, "chunk_inventory")
	);
	public static final ComponentIdentifier<FloatComponent> SAVE_FLOAT =
			FloatComponent.create(new Identifier(MODID, "save_float"));

	// Attention do NOT place this block in any world because registry sync issues will make the game hung upon rejoining.
	public static final Block TEST_BLOCK = new TestBlock(AbstractBlock.Settings.copy(Blocks.STONE));

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registry.BLOCK, new Identifier(MODID, "test_block"), TEST_BLOCK);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MODID, "block_entity"), TEST_BE_TYPE);
		Block.STATE_IDS.add(TEST_BLOCK.getDefaultState());
		// Application Code
		Components.inject(CreeperEntity.class, CREEPER_EXPLODE_TIME);
		Components.injectInheritage(CowEntity.class, COW_INVENTORY);
		Components.injectInheritanceExcept(HostileEntity.class, HOSTILE_EXPLODE_TIME, CreeperEntity.class);
		Components.inject(ChestBlockEntity.class, CHEST_NUMBER);
		Components.injectInheritage(Chunk.class, CHUNK_INVENTORY);
		Components.inject(LevelProperties.class, SAVE_FLOAT);

		// Testing Code
		ServerWorldTickEvents.START.register((server, world) -> {
			world.getEntitiesByType(TypeFilter.instanceOf(CowEntity.class), cowEntity -> true).forEach(entity ->
					entity.expose(COW_INVENTORY).ifPresent(inventoryComponent -> {
						if (inventoryComponent.isEmpty()) {
							world.createExplosion(
									entity,
									entity.getX(), entity.getY(), entity.getZ(),
									4.0f, Explosion.DestructionType.NONE
							);
							entity.discard();
						} else {
							inventoryComponent.removeStack(0, 1);
						}
					}));

			world.getEntitiesByType(EntityType.CREEPER, creeper -> true)
					.forEach(creeper -> Components.expose(CREEPER_EXPLODE_TIME, creeper).ifPresent(explodeTime -> {
						if (explodeTime.get() > 0) {
							explodeTime.decrement();
						} else {
							creeper.ignite();
						}
					}));

			world.getEntitiesByType(TypeFilter.instanceOf(HostileEntity.class), hostile -> true)
					.forEach(hostile -> hostile.expose(HOSTILE_EXPLODE_TIME).ifPresent(explodeTime -> {
						if (explodeTime.get() <= 200) {
							explodeTime.increment();
						} else {
							hostile.getWorld().createExplosion(
									null,
									hostile.getX(), hostile.getY(), hostile.getZ(),
									1.0f, Explosion.DestructionType.NONE
							);
							hostile.discard();
						}
					}));

			ServerPlayerEntity player = world.getRandomAlivePlayer();
			if (player == null) {
				return;
			}
			Chunk chunk = world.getChunk(player.getBlockPos());
			chunk.getBlockEntityPositions().stream()
					.map(chunk::getBlockEntity)
					.filter(Objects::nonNull)
					.forEach(blockEntity -> blockEntity.expose(CHEST_NUMBER).ifPresent(integerComponent -> {
						integerComponent.decrement();

						if (integerComponent.get() <= 0) {
							world.setBlockState(blockEntity.getPos(), Blocks.DIAMOND_BLOCK.getDefaultState());
						}
					}));
			chunk.expose(CHUNK_INVENTORY).ifPresent(inventory -> {
				ItemStack playerStack = player.getInventory().getStack(9);
				ItemStack stack = inventory.getStack(0);
				if (!playerStack.isEmpty()) {
					if (stack.isEmpty()) {
						var newStack = playerStack.copy();
						newStack.setCount(1);
						inventory.setStack(0, newStack);
						playerStack.decrement(1);
					} else {
						if (ItemStack.canCombine(stack, playerStack)) {
							stack.increment(1);
							playerStack.decrement(1);
							inventory.saveNeeded();
						}
					}
				}
				player.sendMessage(Text.literal(inventory.getStack(0).toString()), true);
			});

			LevelProperties props = ((LevelProperties) server.getSaveProperties());
			props.expose(SAVE_FLOAT).ifPresent(floatComponent -> {
				floatComponent.set(floatComponent.get() + 0.5f);
				if (world.getTime() % 100 == 0) {
					player.sendMessage(Text.literal("%.3f".formatted(floatComponent.get())), false);
				}
			});
		});
	}

	public static final BlockEntityType<TestBlockEntity> TEST_BE_TYPE =
			BlockEntityType.Builder.create(TestBlockEntity::new, TEST_BLOCK, Blocks.NOTE_BLOCK).build(null);

}
