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

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

/**
 * Events indicating progress through the tick loop of a Minecraft server.
 *
 * <p>Events in the class are useful for mods which may need to do processing before or after tick of a Minecraft
 * server.
 *
 * <h2>A note of warning</h2>
 * <p>
 * Callbacks registered to any of these events should ensure as little time as possible is spent executing, since the tick
 * loop is a very hot code path.
 */
public final class ServerTickEvents {
	/**
	 * An event indicating an iteration of the server's tick loop will start.
	 */
	public static final Event<Start> START = Event.create(Start.class, callbacks -> server -> {
		for (var callback : callbacks) {
			callback.startServerTick(server);
		}
	});

	/**
	 * An event indicating the server has finished an iteration of the tick loop.
	 *
	 * <p>Since there will be a time gap before the next tick, this is a great spot to run any asynchronous operations
	 * for the next tick.
	 */
	public static final Event<End> END = Event.create(End.class, callbacks -> server -> {
		for (var callback : callbacks) {
			callback.endServerTick(server);
		}
	});

	private ServerTickEvents() {
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #START}.
	 *
	 * @see #START
	 */
	@FunctionalInterface
	public interface Start extends EventAwareListener {
		/**
		 * Called before the server has started an iteration of the tick loop.
		 *
		 * @param server the server
		 */
		void startServerTick(MinecraftServer server);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #END}.
	 *
	 * @see #END
	 */
	@FunctionalInterface
	public interface End extends EventAwareListener {
		/**
		 * Called at the end of an iteration of the server's tick loop.
		 *
		 * @param server the server that finished ticking
		 */
		void endServerTick(MinecraftServer server);
	}
}
