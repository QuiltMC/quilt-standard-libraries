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
 * Events related to a ticking Minecraft server's worlds.
 *
 * <h2>A note of warning</h2>
 * <p>
 * Callbacks registered to any of these events should ensure as little time as possible is spent executing, since the tick
 * loop is a very hot code path.
 */
public final class ServerWorldTickEvents {
	/**
	 * An event indicating that a world will be ticked.
	 */
	public static final Event<Start> START = Event.create(Start.class, callbacks -> (server, world) -> {
		for (var callback : callbacks) {
			callback.startWorldTick(server, world);
		}
	});

	/**
	 * An event indicating that a world has finished being ticked.
	 */
	public static final Event<End> END = Event.create(End.class, callbacks -> (server, world) -> {
		for (var callback : callbacks) {
			callback.endWorldTick(server, world);
		}
	});

	private ServerWorldTickEvents() {
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #START}.
	 *
	 * @see #START
	 */
	@FunctionalInterface
	public interface Start extends EventAwareListener {
		/**
		 * Called before a world is ticked.
		 *
		 * @param server the server
		 * @param world  the world being ticked
		 */
		void startWorldTick(MinecraftServer server, ServerWorld world);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #END}.
	 *
	 * @see #END
	 */
	@FunctionalInterface
	public interface End extends EventAwareListener {
		/**
		 * Called after a world is ticked.
		 *
		 * @param server the server
		 * @param world  the world that was ticked
		 */
		void endWorldTick(MinecraftServer server, ServerWorld world);
	}
}
