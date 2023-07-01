/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.worldgen.dimension;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.Blocks;
import net.minecraft.command.CommandBuildContext;
import net.minecraft.command.CommandException;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.TestServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.worldgen.dimension.api.QuiltDimensions;

public class QuiltDimensionTest implements ModInitializer, ServerLifecycleEvents.Ready, CommandRegistrationCallback {
	private static final RegistryKey<DimensionOptions> DIMENSION_KEY = RegistryKey.of(RegistryKeys.DIMENSION,
			new Identifier("quilt_dimension", "void")
	);

	private static RegistryKey<World> WORLD_KEY = RegistryKey.of(RegistryKeys.WORLD, DIMENSION_KEY.getValue());

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registries.CHUNK_GENERATOR, new Identifier("quilt_dimension", "void"), EmptyChunkGenerator.CODEC);

		WORLD_KEY = RegistryKey.of(RegistryKeys.WORLD, new Identifier("quilt_dimension", "void"));
	}

	@Override
	public void readyServer(MinecraftServer server) {
		if (server instanceof TestServer) return; // Game Test server does not support custom dimensions.

		ServerWorld overworld = server.getWorld(World.OVERWORLD);
		ServerWorld targetWorld = server.getWorld(WORLD_KEY);

		if (targetWorld == null) {
			throw new AssertionError("Test world somehow doesn't exist.");
		}

		CowEntity cow = EntityType.COW.create(overworld);

		if (!cow.getWorld().getRegistryKey().equals(World.OVERWORLD)) {
			throw new AssertionError("Cow was spawned but isn't in the overworld.");
		}

		var target = new TeleportTarget(Vec3d.ZERO, new Vec3d(1, 1, 1), 45f, 60f);
		CowEntity teleportedEntity = QuiltDimensions.teleport(cow, targetWorld, target);

		if (teleportedEntity == null || !teleportedEntity.getWorld().getRegistryKey().equals(WORLD_KEY)) {
			throw new AssertionError("Cow was not teleported correctly.");
		}

		if (!teleportedEntity.getPos().equals(target.position)) {
			throw new AssertionError("Cow was moved to different world, but not to the correct location.");
		}
	}

	@Override
	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandBuildContext buildContext,
			CommandManager.RegistrationEnvironment environment) {
		dispatcher.register(CommandManager.literal("quilt_dimension_test").executes(this::swapTargeted));
	}

	private int swapTargeted(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayer();
		ServerWorld serverWorld = (ServerWorld) player.getWorld();
		ServerWorld modWorld = context.getSource().getServer().getWorld(WORLD_KEY);

		if (serverWorld != modWorld) {
			var target = new TeleportTarget(new Vec3d(0.5, 101, 0.5), Vec3d.ZERO, 0, 0);
			QuiltDimensions.teleport(player, modWorld, target);

			if (player.getWorld() != modWorld) {
				throw new CommandException(Text.literal("Teleportation failed!"));
			}

			modWorld.setBlockState(new BlockPos(0, 100, 0), Blocks.DIAMOND_BLOCK.getDefaultState());
			modWorld.setBlockState(new BlockPos(0, 101, 0), Blocks.TORCH.getDefaultState());
		} else {
			var target = new TeleportTarget(new Vec3d(0, 100, 0), Vec3d.ZERO,
					(float) Math.random() * 360 - 180, (float) Math.random() * 360 - 180);
			QuiltDimensions.teleport(player, context.getSource().getServer().getWorld(World.END), target);
		}

		return 1;
	}
}
