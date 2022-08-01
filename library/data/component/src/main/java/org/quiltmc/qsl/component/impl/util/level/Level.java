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

package org.quiltmc.qsl.component.impl.util.level;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.MinecraftServer;

import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.api.sync.SyncChannel;

public final class Level implements ComponentProvider {
	private final boolean isClient;
	private final ComponentContainer container;
	@Nullable
	private MinecraftServer server;

	private Level(boolean isClient) {
		this.isClient = isClient;
		var builder = ComponentContainer.builder(this);
		if (!this.isClient) {
			builder.syncing(SyncChannel.LEVEL).ticking().saving(this::save).build(ComponentContainer.LAZY_FACTORY);
		}
		this.container = builder.build(ComponentContainer.LAZY_FACTORY);
	}

	public static Level createClient() {
		return new Level(true);
	}

	public static Level createServer(MinecraftServer server) {
		Level level = new Level(false);
		level.server = server;
		return level;
	}

	public void save() {
	}

	@Override
	public ComponentContainer getComponentContainer() {
		return this.container;
	}
}
