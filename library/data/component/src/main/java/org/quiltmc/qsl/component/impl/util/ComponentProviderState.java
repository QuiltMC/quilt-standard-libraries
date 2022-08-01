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

package org.quiltmc.qsl.component.impl.util;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.api.sync.SyncChannel;
import org.quiltmc.qsl.component.impl.container.LazyComponentContainer;

// TODO: Fix this
public class ComponentProviderState extends PersistentState implements ComponentProvider {
	public static final String ID = "components";
	public static final String GLOBAL_ID = "save_components";

	private final ComponentContainer container;

	public ComponentProviderState(ServerWorld world, boolean level) {
		this.container = level ? this.initLevelContainer(world.getServer()) : this.initWorldContainer(world);
	}

	public ComponentProviderState(NbtCompound rootQslNbt, ServerWorld world) {
		this.container = this.initWorldContainer(world);
		this.container.readNbt(rootQslNbt);
	}

	public static ComponentProvider get(World world) {
		return !world.isClient ? ((ServerWorld) world).getPersistentStateManager().getOrCreate(
				nbtCompound -> new ComponentProviderState(nbtCompound, (ServerWorld) world),
				() -> new ComponentProviderState((ServerWorld) world, false),
				ID
		) : ClientComponentProviderState.getOrCreate(world);
	}

	public static ComponentProvider getGlobal(MinecraftServer server) {
		ServerWorld overworld = server.getOverworld();
		return overworld.getPersistentStateManager().getOrCreate(
				nbtCompound -> new ComponentProviderState(nbtCompound, overworld),
				() -> new ComponentProviderState(overworld, true),
				GLOBAL_ID
		);
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		this.container.writeNbt(nbt);
		return nbt;
	}

	@Override
	public ComponentContainer getComponentContainer() {
		return this.container;
	}

	private LazyComponentContainer initLevelContainer(MinecraftServer server) {
		return ComponentContainer.builder(server)
				.saving(this::markDirty)
				.syncing(SyncChannel.LEVEL)
				.ticking()
				.acceptsInjections()
				.build(ComponentContainer.LAZY_FACTORY);
	}

	private LazyComponentContainer initWorldContainer(ServerWorld world) {
		return ComponentContainer.builder(world)
				.saving(this::markDirty)
				.syncing(SyncChannel.WORLD)
				.ticking()
				.acceptsInjections()
				.build(ComponentContainer.LAZY_FACTORY);
	}

	public static class ClientComponentProviderState implements ComponentProvider {
		private static final Map<RegistryKey<World>, ComponentProvider> cachedValue = new HashMap<>();
		private final ComponentContainer container;

		private ClientComponentProviderState(World world) {
			this.container = ComponentContainer.builder(world).build(ComponentContainer.LAZY_FACTORY);
		}

		private static ComponentProvider getOrCreate(World world) {
			return cachedValue.computeIfAbsent(
					world.getRegistryKey(),
					worldRegistryKey -> new ClientComponentProviderState(world)
			);
		}

		@Override
		public ComponentContainer getComponentContainer() {
			return this.container;
		}
	}
}
