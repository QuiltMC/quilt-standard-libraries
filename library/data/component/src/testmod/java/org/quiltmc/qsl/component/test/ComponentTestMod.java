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

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.explosion.Explosion;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.api.components.IntegerComponent;
import org.quiltmc.qsl.component.api.components.InventoryComponent;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;

public class ComponentTestMod implements ModInitializer {
	public static final String MODID = "quilt_component_test";

	// Registration Code
	public static final ComponentIdentifier<InventoryComponent> COW_INVENTORY = InventoryComponent.of(
			() -> DefaultedList.ofSize(1, new ItemStack(Items.COBBLESTONE, 64)),
			new Identifier("quilt_component_test", "cow_inventory")
	);
	public static final ComponentIdentifier<IntegerComponent> CREEPER_EXPLODE_TIME =
			IntegerComponent.create(200, new Identifier(MODID, "creeper_explode_time"));
	public static final ComponentIdentifier<IntegerComponent> HOSTILE_EXPLODE_TIME =
			IntegerComponent.create(new Identifier(MODID, "hostile_explode_time"));
	public static final ComponentIdentifier<IntegerComponent> CHEST_NUMBER =
			IntegerComponent.create(new Identifier(MODID, "chest_number"));

	@Override
	public void onInitialize(ModContainer mod) {
		// Application Code
		Components.inject(CreeperEntity.class, CREEPER_EXPLODE_TIME);
		Components.injectInheritage(CowEntity.class, COW_INVENTORY);
		Components.injectInheritanceExcept(HostileEntity.class, HOSTILE_EXPLODE_TIME, CreeperEntity.class);
		Components.inject(ChestBlockEntity.class, CHEST_NUMBER);

		// Testing Code
		ServerWorldTickEvents.START.register((ignored, world) -> world.iterateEntities().forEach(entity -> {
			if (entity instanceof CowEntity) {
				Components.expose(COW_INVENTORY, entity).ifPresent(inventoryComponent -> {
					if (inventoryComponent.isEmpty()) {
						entity.discard();
						world.createExplosion(
								entity,
								entity.getX(), entity.getY(), entity.getZ(),
								4.0f, Explosion.DestructionType.NONE
						);
					} else {
						inventoryComponent.removeStack(0, 1);
					}
				});
			} else if (entity instanceof CreeperEntity creeper) {
				Components.expose(CREEPER_EXPLODE_TIME, creeper).ifPresent(explodeTime -> {
					if (explodeTime.get() > 0) {
						explodeTime.decrement();
					} else {
						creeper.ignite();
					}
				});
			} else if (entity instanceof HostileEntity hostile) {
				Components.expose(HOSTILE_EXPLODE_TIME, hostile).ifPresent(explodeTime -> {
					if (explodeTime.get() <= 200) {
						explodeTime.increment();
					} else {
						hostile.discard();
						hostile.getWorld().createExplosion(null, hostile.getX(), hostile.getY(), hostile.getZ(), 1.0f, Explosion.DestructionType.NONE);
					}
				});
			}
		}));


		ServerWorldTickEvents.START.register((server, world) ->
				BlockPos.streamOutwards(new BlockPos(0, 0, 0), 5, 5, 5).forEach(pos -> {
					BlockEntity blockEntity = world.getBlockEntity(pos);
					if (blockEntity instanceof ChestBlockEntity chest) {
						Components.expose(CHEST_NUMBER, chest)
								.ifPresent(integerComponent -> {
									if (integerComponent.get() == 0) {
										integerComponent.set(world.random.nextInt(10));
									} else if (integerComponent.get() == 6) {
										world.setBlockState(pos, Blocks.DIAMOND_BLOCK.getDefaultState());
									}
								});
					}
				})
		);
	}

}
