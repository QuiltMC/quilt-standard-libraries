/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.lifecycle.api.event;

import org.quiltmc.qsl.base.api.event.ArrayEvent;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

/**
 * Events indicating whether a world has been loaded or unloaded from a server.
 *
 * <p>Mods which implement dynamically loading and unloading worlds from a server may execute these events to allow
 * mods to initialize state on new worlds or clean up references to a world.
 */
public final class ServerWorldLoadEvents {
	/**
	 * Called when a world is loaded onto a Minecraft server.
	 *
	 * <p>This event is typically executed at server initialization, but may be called at any time if any mods dynamically
	 * load worlds.
	 */
	public static final ArrayEvent<Load> LOAD = ArrayEvent.create(Load.class, callbacks -> (server, world) -> {
		for (var callback : callbacks) {
			callback.loadWorld(server, world);
		}
	});

	/**
	 * Called when a world is unloaded from a Minecraft server.
	 *
	 * <p>This event is typically executed at server shutdown, but may be called at any time if any mods dynamically
	 * unload worlds.
	 */
	public static final ArrayEvent<Unload> UNLOAD = ArrayEvent.create(Unload.class, callbacks -> ((server, world) -> {
		for (var callback : callbacks) {
			callback.unloadWorld(server, world);
		}
	}));

	private ServerWorldLoadEvents() {}

	/**
	 * Functional interface to be implemented on callbacks for {@link #LOAD}.
	 * @see #LOAD
	 */
	@FunctionalInterface
	public interface Load {
		void loadWorld(MinecraftServer server, ServerWorld world);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #UNLOAD}.
	 * @see #UNLOAD
	 */
	@FunctionalInterface
	public interface Unload {
		void unloadWorld(MinecraftServer server, ServerWorld world);
	}
}
