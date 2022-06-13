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

import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.api.components.IntegerComponent;
import org.quiltmc.qsl.component.api.components.InventoryComponent;
import org.quiltmc.qsl.component.api.identifier.ComponentIdentifier;
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents;

import java.util.Optional;

public class ComponentTestMod implements ModInitializer {
	public static final String MODID = "quilt_component_test";

	// Registration Code
	public static final ComponentIdentifier<InventoryComponent> COW_INVENTORY = InventoryComponent.of(
			() -> DefaultedList.ofSize(1, new ItemStack(Items.COBBLESTONE, 64)),
			new Identifier("quilt_component_test", "cow_inventory")
	);
	public static final ComponentIdentifier<IntegerComponent> CREEPER_DYED_TIME =
			IntegerComponent.create(200, new Identifier(MODID, "creeper_dye_time"));

	@Override
	public void onInitialize(ModContainer mod) {
		// Application Code
		Components.inject(CreeperEntity.class, CREEPER_DYED_TIME);
		Components.injectInheritage(CowEntity.class, COW_INVENTORY);

		// Testing Code
		ServerTickEvents.START.register(server -> {
			ServerWorld world = server.getWorld(World.OVERWORLD);
			assert world != null;
			world.iterateEntities().forEach(entity -> {
				if (entity instanceof CowEntity) {
					Components.expose(COW_INVENTORY, entity)
							.ifPresent(inventoryComponent -> {
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
					Optional<IntegerComponent> exposed = Components.expose(CREEPER_DYED_TIME, creeper);
					exposed.ifPresent(IntegerComponent::decrement);
					exposed.filter(it -> it.get() <= 0).ifPresent(ignored -> creeper.ignite());
				}
			});
		});
	}

}
