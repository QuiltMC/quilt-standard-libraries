package org.quiltmc.qsl.component.test;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypeFilter;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.explosion.Explosion;
import org.quiltmc.qsl.base.api.event.ListenerPhase;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.api.components.IntegerComponent;
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
		ItemStack stackInHand = player.getStackInHand(Hand.MAIN_HAND);

		cowTick(world);
		creeperTick(world);
		hostileTick(world);
		currentChunkBETick(world, chunk);
		currentChunkTick(player, chunk);
		stackInHandTick(player, stackInHand);
	}

	private void stackInHandTick(ServerPlayerEntity player, ItemStack stackInHand) {
		if (!stackInHand.isEmpty() && stackInHand.isOf(Items.BOOKSHELF)) {
			stackInHand.expose(ComponentTestMod.ITEMSTACK_INT).ifPresent(integerComponent -> {
				integerComponent.increment();

				if (integerComponent.get() >= 200) {
					player.setStackInHand(Hand.MAIN_HAND, new ItemStack(Items.BOOK, 12));
				}
			});
		}
	}

	private void currentChunkTick(ServerPlayerEntity player, Chunk chunk) {
		chunk.expose(ComponentTestMod.CHUNK_INVENTORY).ifPresent(inventory -> {
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
						stack.expose(ComponentTestMod.ITEMSTACK_INT).ifPresent(IntegerComponent::increment);
						inventory.saveNeeded();
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
				.forEach(blockEntity -> blockEntity.expose(ComponentTestMod.CHEST_NUMBER).ifPresent(integerComponent -> {
					integerComponent.decrement();

					if (integerComponent.get() <= 0) {
						world.setBlockState(blockEntity.getPos(), Blocks.DIAMOND_BLOCK.getDefaultState());
					}
				}));
	}

	private void hostileTick(ServerWorld world) {
		world.getEntitiesByType(TypeFilter.instanceOf(HostileEntity.class), hostile -> true)
				.forEach(hostile -> hostile.expose(ComponentTestMod.HOSTILE_EXPLODE_TIME).ifPresent(explodeTime -> {
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
	}

	private void creeperTick(ServerWorld world) {
		world.getEntitiesByType(EntityType.CREEPER, creeper -> true)
				.forEach(creeper -> Components.expose(ComponentTestMod.CREEPER_EXPLODE_TIME, creeper).ifPresent(explodeTime -> {
					if (explodeTime.get() > 0) {
						explodeTime.decrement();
					} else {
						creeper.ignite();
					}
				}));
	}

	private void cowTick(ServerWorld world) {
		world.getEntitiesByType(TypeFilter.instanceOf(CowEntity.class), cowEntity -> true).forEach(entity ->
				entity.expose(ComponentTestMod.COW_INVENTORY).ifPresent(inventoryComponent -> {
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
	}
}
