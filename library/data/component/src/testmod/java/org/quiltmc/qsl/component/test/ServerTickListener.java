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
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.TypeFilter;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.explosion.Explosion;
import org.quiltmc.qsl.base.api.event.ListenerPhase;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.api.component.SyncedComponent;
import org.quiltmc.qsl.component.impl.component.DefaultInventoryComponent;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;

import java.util.Objects;

@ListenerPhase(
		callbackTarget = ServerWorldTickEvents.End.class,
		namespace = ComponentTestMod.MODID, path = "component_test_tick"
)
public class ServerTickListener implements ServerWorldTickEvents.End {
	@Override
	public void endWorldTick(MinecraftServer server, ServerWorld world) {
		ServerPlayerEntity player = world.getRandomAlivePlayer();
		if (player == null) {
			return;
		}
		Chunk chunk = world.getChunk(player.getBlockPos());

		cowTick(world);
		creeperTick(world);
		hostileTick(world);
		currentChunkBETick(world, chunk);
		currentChunkTick(player, chunk);
	}

	private void currentChunkTick(ServerPlayerEntity player, Chunk chunk) {
		chunk.expose(ComponentTestMod.CHUNK_INVENTORY).ifJust(inventory -> {
			ItemStack playerStack = player.getInventory().getStack(9);
			ItemStack stack = inventory.getStack(0);
			if (!playerStack.isEmpty()) {
				if (stack.isEmpty()) {
					var newStack = playerStack.copy();
					newStack.setCount(1);
					inventory.setStack(0, newStack);
					inventory.save();
					inventory.sync();
					playerStack.decrement(1);
				} else {
					if (ItemStack.canCombine(stack, playerStack)) {
						stack.increment(1);
						playerStack.decrement(1);
//						stack.expose(ComponentTestMod.ITEMSTACK_INT).ifJust(defaultIntegerComponent -> {
//							defaultIntegerComponent.increment();
//							defaultIntegerComponent.save();
//						});
						inventory.save();
						inventory.sync();
					}
				}
			}
			player.sendMessage(Text.literal(inventory.getStack(0).toString()), true);
		});
	}

	private void currentChunkBETick(ServerWorld world, Chunk chunk) {
		chunk.getBlockEntityPositions().stream()
				.map(chunk::getBlockEntity)
				.filter(Objects::nonNull)
				.forEach(blockEntity -> blockEntity.expose(ComponentTestMod.CHEST_NUMBER).ifJust(integerComponent -> {
					integerComponent.decrement();
					integerComponent.save();

					if (integerComponent.get() <= 0) {
						world.setBlockState(blockEntity.getPos(), Blocks.DIAMOND_BLOCK.getDefaultState());
					}
				}));
	}

	private void hostileTick(ServerWorld world) {
		world.getEntitiesByType(TypeFilter.instanceOf(HostileEntity.class), hostile -> true)
				.forEach(hostile -> hostile.expose(ComponentTestMod.HOSTILE_EXPLODE_TIME).ifJust(explodeTime -> {
					if (explodeTime.get() <= 200) {
						explodeTime.increment();
						explodeTime.save();
						// explodeTime.sync(); // Causes mucho lag!!
					} else {
						hostile.getWorld().createExplosion(
								null,
								hostile.getX(), hostile.getY(), hostile.getZ(),
								1.0f, Explosion.DestructionType.NONE
						);
						hostile.discard();
					}
				}));
	}

	private void creeperTick(ServerWorld world) {
		world.getEntitiesByType(EntityType.CREEPER, creeper -> true)
				.forEach(creeper -> Components.expose(ComponentTestMod.CREEPER_EXPLODE_TIME, creeper).ifJust(explodeTime -> {
					if (explodeTime.get() > 0) {
						explodeTime.decrement();
						explodeTime.save();
					} else {
						creeper.ignite();
					}
				}));
	}

	private void cowTick(ServerWorld world) {
		world.getEntitiesByType(TypeFilter.instanceOf(CowEntity.class), cowEntity -> true).forEach(entity ->
				entity.expose(ComponentTestMod.COW_INVENTORY).ifJust(inventoryComponent -> {
					if (inventoryComponent.isEmpty()) {
						world.createExplosion(
								entity,
								entity.getX(), entity.getY(), entity.getZ(),
								4.0f, Explosion.DestructionType.NONE
						);
						entity.discard();
					} else {
						inventoryComponent.removeStack(0, 1);
						inventoryComponent.save();
					}
				}));
	}
}
