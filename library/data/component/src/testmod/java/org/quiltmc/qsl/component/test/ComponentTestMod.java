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

import java.util.UUID;

import com.mojang.serialization.Codec;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.api.component.Tickable;
import org.quiltmc.qsl.component.api.component.field.SyncedSavedField;
import org.quiltmc.qsl.component.api.injection.ComponentInjector;
import org.quiltmc.qsl.component.api.sync.codec.NetworkCodec;
import org.quiltmc.qsl.component.test.component.ChunkInventorySerializable;
import org.quiltmc.qsl.component.test.component.DefaultIntegerSerializable;
import org.quiltmc.qsl.component.test.component.DefaultInventorySerializable;
import org.quiltmc.qsl.component.test.component.InventorySerializable;
import org.quiltmc.qsl.component.test.component.SaveFloatSerializable;

public class ComponentTestMod implements ModInitializer {
	public static final String MOD_ID = "quilt_component_test";

	public static final ComponentType<InventorySerializable> COW_INVENTORY = Components.register(
			new Identifier(MOD_ID, "cow_inventory"),
			operations -> new DefaultInventorySerializable(
					operations, () -> DefaultedList.ofSize(1, new ItemStack(Items.COBBLESTONE, 64))
			)
	);
	public static final ComponentType<DefaultIntegerSerializable> CREEPER_EXPLODE_TIME = Components.register(
			new Identifier(MOD_ID, "creeper_explode_time"),
			operations -> new DefaultIntegerSerializable(operations, 200)
	);
	public static final ComponentType<DefaultIntegerSerializable> HOSTILE_EXPLODE_TIME = Components.register(
			new Identifier(MOD_ID, "hostile_explode_time"),
			DefaultIntegerSerializable::new
	);
	public static final ComponentType<SyncedSavedField<Integer>> CHEST_NUMBER = Components.register(
			new Identifier(MOD_ID, "chest_number"),
			operations -> new SyncedSavedField<>(operations, Codec.INT, NetworkCodec.VAR_INT, 200)
	);
	public static final ComponentType<ChunkInventorySerializable> CHUNK_INVENTORY = Components.register(
			new Identifier(MOD_ID, "chunk_inventory"),
			ChunkInventorySerializable::new
	);
	public static final ComponentType<SaveFloatSerializable> SAVE_FLOAT = Components.registerInstant(
			new Identifier(MOD_ID, "save_float"),
			SaveFloatSerializable::new
	);
	public static final Block TEST_BLOCK = new TestBlock(AbstractBlock.Settings.copy(Blocks.STONE));
	public static final ComponentType<DefaultIntegerSerializable> TEST_BE_INT = Components.register(
			new Identifier(ComponentTestMod.MOD_ID, "test_be_int"),
			DefaultIntegerSerializable::new
	);
	public static final ComponentType<SyncedSavedField<UUID>> UUID_THING = Components.register(
			new Identifier(MOD_ID, "uuid_thing"),
			(ops) -> new SyncedSavedField<>(ops, Codecs.UUID, NetworkCodec.UUID)
	);
	public static final ComponentType<Tickable> PLAYER_TICK = Components.registerInstant(
			new Identifier(MOD_ID, "player_tick"),
			(ops) -> provider -> {
				if (provider instanceof ServerPlayerEntity player) {
					ItemStack stackInHand = player.getStackInHand(Hand.MAIN_HAND);
					if (stackInHand.isOf(Items.WHEAT)) {
						player.giveItemStack(new ItemStack(Items.WHEAT));
						player.sendMessage(Text.literal("Prankt"), true);
					}

					var props = player.getWorld().getServer().getSaveProperties();
					if (props instanceof MinecraftServer server && player.getWorld().getTime() % 100 == 0) {
						server.ifPresent(
								SAVE_FLOAT,
								saveFloatSerializable -> player.sendMessage(
										Text.literal(String.valueOf(saveFloatSerializable.get())), false
								)
						);
					}

					player.ifPresent(UUID_THING, uuidGenericComponent -> {
						Entity vehicle = player.getVehicle();

						if (vehicle != null) {
							if (uuidGenericComponent.getValue() == null) {
								uuidGenericComponent.setValue(vehicle.getUuid());
								uuidGenericComponent.save();
								uuidGenericComponent.sync();
							} else {
								Entity vehicle1 = player.getWorld().getEntity(uuidGenericComponent.getValue());

								if (vehicle1 == null) {
									uuidGenericComponent.setValue(null);
									uuidGenericComponent.save();
									uuidGenericComponent.sync();
									return;
								}

								player.getWorld().setBlockState(
										vehicle1.getBlockPos().down(),
										Blocks.DIAMOND_BLOCK.getDefaultState()
								);
							}
						} else {
							uuidGenericComponent.setValue(null);
							uuidGenericComponent.save();
							uuidGenericComponent.sync();
						}
					});
				}
			}
	);

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "test_block"), TEST_BLOCK);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "block_entity"), TEST_BE_TYPE);
		Block.STATE_IDS.add(TEST_BLOCK.getDefaultState());

		// Cached Injection
		ComponentInjector.injector(CreeperEntity.class)
						 .entry(CREEPER_EXPLODE_TIME).add()
						 .inject();
		ComponentInjector.injector(Chunk.class)
						 .entry(CHUNK_INVENTORY).factory(ChunkInventorySerializable::new).add()
						 .inherited()
						 .inject();
		ComponentInjector.injector(HostileEntity.class)
						 .entry(HOSTILE_EXPLODE_TIME).add()
						 .inherited()
						 .exclude(CreeperEntity.class)
						 .inject();
		ComponentInjector.injector(ChestBlockEntity.class)
						 .entry(CHEST_NUMBER).add()
						 .inject();
		ComponentInjector.injector(CowEntity.class)
						 .entry(COW_INVENTORY).add()
						 .inject();
		ComponentInjector.injector(ServerPlayerEntity.class)
						 .entry(PLAYER_TICK).add()
						 .inject();
		ComponentInjector.injector(PlayerEntity.class)
						 .entry(UUID_THING).add()
						 .inherited()
						 .inject();
		ComponentInjector.injector(World.class)
						 .entry(SAVE_FLOAT).add()
						 .inherited()
						 .inject();
	}

	public static final BlockEntityType<TestBlockEntity> TEST_BE_TYPE =
			QuiltBlockEntityTypeBuilder.create(TestBlockEntity::new, TEST_BLOCK).build();
}
