/*
 * Copyright 2021 The Quilt Project
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

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

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
	 * load worlds. Mods which wish to add worlds while the server are running are expected to execute this event.
	 */
	public static final Event<Load> LOAD = Event.create(Load.class, callbacks -> (server, world) -> {
		for (var callback : callbacks) {
			callback.loadWorld(server, world);
		}
	});

	/**
	 * Called when a world is unloaded from a Minecraft server.
	 *
	 * <p>This event is typically executed at server shutdown, but may be called at any time if any mods dynamically
	 * unload worlds. Mods which wish to remove worlds while the server are running are expected to execute this event.
	 */
	public static final Event<Unload> UNLOAD = Event.create(Unload.class, callbacks -> ((server, world) -> {
		for (var callback : callbacks) {
			callback.unloadWorld(server, world);
		}
	}));

	private ServerWorldLoadEvents() {
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #LOAD}.
	 *
	 * @see #LOAD
	 */
	@FunctionalInterface
	public interface Load extends EventAwareListener {
		/**
		 * Called when a world is loaded onto a Minecraft server.
		 *
		 * <p>Mods which maintain per world state may use this event to initialize any required state for this world.
		 *
		 * @param server the server the world belongs to
		 * @param world  the world that was loaded
		 */
		void loadWorld(MinecraftServer server, ServerWorld world);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #UNLOAD}.
	 *
	 * @see #UNLOAD
	 */
	@FunctionalInterface
	public interface Unload extends EventAwareListener {
		/**
		 * Called when a world is unloaded from a Minecraft server.
		 *
		 * <p>Mods which maintain per world state should save and clean up any state they have attached to the world.
		 *
		 * @param server the server the world was unloaded from
		 * @param world  the world which was unloaded
		 */
		void unloadWorld(MinecraftServer server, ServerWorld world);
	}
}
