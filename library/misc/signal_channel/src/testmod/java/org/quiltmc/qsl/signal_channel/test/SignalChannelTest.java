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

package org.quiltmc.qsl.signal_channel.test;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder;
import org.quiltmc.qsl.signal_channel.api.SignalChannel;

public class SignalChannelTest implements ModInitializer {
	public static SignalChannel<Unit> REGENERATION_CHANNEL = SignalChannel.createUnnamed();
	public static BlockEntityType<RegenerationBlock.Entity> REGENERATION_ENTITY_TYPE;

	@Override
	public void onInitialize(ModContainer mod) {
		var regenBlock = Registry.register(
				Registry.BLOCK,
				new Identifier("quilt_signal_channel_testmod", "regeneration_block"),
				new RegenerationBlock()
		);

		Registry.register(
				Registry.ITEM,
				new Identifier("quilt_signal_channel_testmod", "regeneration_block"),
				new BlockItem(regenBlock, new Item.Settings())
		);

		REGENERATION_ENTITY_TYPE = Registry.register(
				Registry.BLOCK_ENTITY_TYPE,
				new Identifier("quilt_signal_channel_testmod", "regeneration_block_entity"),
				QuiltBlockEntityTypeBuilder.create(
						RegenerationBlock.Entity::new,
						regenBlock
				).build()
		);
	}
}

class RegenerationBlock extends BlockWithEntity {
	RegenerationBlock() {
		super(AbstractBlock.Settings.of(Material.STONE));
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new Entity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return (entityWorld, blockPos, blockState, blockEntity) -> {
			if (world.getTime() % 40 == 0) {
				SignalChannelTest.REGENERATION_CHANNEL.emitInRange(
						Unit.INSTANCE,
						entityWorld,
						Vec3d.of(blockPos),
						4
				);
			}
		};
	}

	static class Entity extends BlockEntity {
		public Entity(BlockPos pos, BlockState state) {
			super(SignalChannelTest.REGENERATION_ENTITY_TYPE, pos, state);
		}
	}
}
