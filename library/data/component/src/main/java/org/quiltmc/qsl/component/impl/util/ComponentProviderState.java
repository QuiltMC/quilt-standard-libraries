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

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.container.LazyComponentContainer;
import org.quiltmc.qsl.component.impl.sync.header.SyncPacketHeader;

public class ComponentProviderState extends PersistentState implements ComponentProvider {
	public static final String ID = "components";
	public static final String GLOBAL_ID = "save_components";

	private final ComponentContainer container;

	public ComponentProviderState(ServerWorld world) {
		this.container = this.initContainer(world);
	}

	public ComponentProviderState(NbtCompound rootQslNbt, ServerWorld world) {
		this.container = this.initContainer(world);
		this.container.readNbt(rootQslNbt);
	}

	public static ComponentProviderState get(Object obj) {
		if (!(obj instanceof ServerWorld world)) {
			throw ErrorUtil.illegalArgument("A ServerWorld instance needs to be provided to initialize a container!").get();
		}

		return world.getPersistentStateManager().getOrCreate(
				nbtCompound -> new ComponentProviderState(nbtCompound, world),
				() -> new ComponentProviderState(world),
				ID
		);
	}

	public static ComponentProviderState getGlobal(Object obj) {
		if (!(obj instanceof MinecraftServer server)) {
			throw ErrorUtil.illegalArgument("A MinecraftServer instance needs to be provided to initialize a container!").get();
		}

		ServerWorld overworld = server.getOverworld();
		return overworld.getPersistentStateManager().getOrCreate(
				nbtCompound -> new ComponentProviderState(nbtCompound, overworld),
				() -> new ComponentProviderState(overworld),
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

	private LazyComponentContainer initContainer(ServerWorld world) {
		return ComponentContainer.builder(this)
				.unwrap()
				.saving(this::markDirty)
				.syncing(SyncPacketHeader.SAVE, world::getPlayers)
				.ticking()
				.build(LazyComponentContainer.FACTORY);
	}
}
