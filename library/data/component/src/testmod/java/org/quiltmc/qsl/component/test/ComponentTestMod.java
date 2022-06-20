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

import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.Entity;
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
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
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
	public static final ComponentIdentifier<InventoryComponent> CHUNK_INVENTORY = InventoryComponent.ofSize(1,
			new Identifier(MODID, "chunk_inventory")
	);

	@Override
	public void onInitialize(ModContainer mod) {
		// Application Code
		Components.injectInheritage(Chunk.class, CHUNK_INVENTORY);
		Components.inject(CreeperEntity.class, CREEPER_EXPLODE_TIME);
		Components.injectInheritage(CowEntity.class, COW_INVENTORY);
		Components.injectInheritanceExcept(HostileEntity.class, HOSTILE_EXPLODE_TIME, CreeperEntity.class);
		Components.inject(ChestBlockEntity.class, CHEST_NUMBER);

		// Testing Code
		ServerWorldTickEvents.START.register((ignored, world) -> {
			world.getEntitiesByType(TypeFilter.instanceOf(CowEntity.class), cowEntity -> true).forEach(entity ->
					Components.expose(COW_INVENTORY, entity).ifPresent(inventoryComponent -> {
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

			world.getEntitiesByType(new TypeFilter<Entity, CreeperEntity>() {
				@Nullable
				@Override
				public CreeperEntity downcast(Entity obj) {
					return obj.getType() == EntityType.CREEPER ? (CreeperEntity) obj : null;
				}

				@Override
				public Class<CreeperEntity> getBaseClass() {
					return CreeperEntity.class;
				}
			}, creeper -> true).forEach(creeper -> Components.expose(CREEPER_EXPLODE_TIME, creeper).ifPresent(explodeTime -> {
				if (explodeTime.get() > 0) {
					explodeTime.decrement();
				} else {
					creeper.ignite();
				}
			}));

			world.getEntitiesByType(TypeFilter.instanceOf(HostileEntity.class), hostile -> true).forEach(hostile -> Components.expose(HOSTILE_EXPLODE_TIME, hostile).ifPresent(explodeTime -> {
				if (explodeTime.get() <= 200) {
					explodeTime.increment();
				} else {
					hostile.getWorld().createExplosion(null, hostile.getX(), hostile.getY(), hostile.getZ(), 1.0f, Explosion.DestructionType.NONE);
					hostile.discard();
				}
			}));

			ServerPlayerEntity player = world.getRandomAlivePlayer();
			if (player == null) {
				return;
			}
			Chunk chunk = world.getChunk(player.getBlockPos());
			Components.expose(CHUNK_INVENTORY, chunk).ifPresent(inventory -> {
				if (player.getInventory().getStack(9).isOf(Items.DIRT)) {
					player.getInventory().getStack(9).decrement(1);
					ItemStack stack = inventory.getStack(0);
					if (stack.isEmpty()) {
						inventory.setStack(0, Items.DIRT.getDefaultStack());
					} else {
						stack.increment(1);
					}
				}
				player.sendMessage(Text.literal(inventory.getStack(0).toString()), false);
			});
		});
	}
}
