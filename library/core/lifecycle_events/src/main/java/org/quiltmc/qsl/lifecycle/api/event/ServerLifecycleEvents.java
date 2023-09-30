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
 * Events indicating the lifecycle of a Minecraft server.
 *
 * <p>The lifecycle of a Minecraft server is simple, the server starts, ticks each world in a loop until the server
 * exits. This lifecycle is consistent across a dedicated server and the integrated server a Minecraft client will run
 * in single player mode.
 *
 * <p>This class only contains methods regarding the starting and exit processes of a Minecraft server, see {@link ServerTickEvents}
 * for events that are called during the tick loop.
 *
 * @see ServerTickEvents
 */
public final class ServerLifecycleEvents {
	// For maintainers: indicate events in this class in order of server starting and then exiting.
	// Ticking events belong in ServerTickEvents.

	// Server initialization

	/**
	 * An event indicating that a Minecraft server is starting.
	 *
	 * <p>This is the first event fired in the lifecycle of a Minecraft server. It should be noted at this point that
	 * the server has no registered player manager, or worlds.
	 */
	public static final Event<Starting> STARTING = Event.create(Starting.class, callbacks -> server -> {
		for (var callback : callbacks) {
			callback.startingServer(server);
		}
	});

	/**
	 * An event indicating that a Minecraft server is ready to tick.
	 *
	 * <p>This event indicates that a Minecraft server is fully initialized and is ready to accept players. Generally
	 * all worlds would have been loaded by the Minecraft server already.
	 *
	 * <p>After this event finishes, the server will tick for the first time.
	 */
	public static final Event<Ready> READY = Event.create(Ready.class, callbacks -> server -> {
		for (var callback : callbacks) {
			callback.readyServer(server);
		}
	});

	// Server exit

	/**
	 * An event indicating that a Minecraft server has finished its last tick and will shut down.
	 *
	 * <p>When this event is executed, the server is in a state similar to the end of a tick, where all worlds are loaded,
	 * and players are still connected to the server.
	 *
	 * <h2>What should mods do when this event is executed</h2>
	 * <p>
	 * Mods may do clean up work when this event is executed, such as shutting down any asynchronous executors,
	 * databases and saving auxiliary mod data.
	 */
	public static final Event<Stopping> STOPPING = Event.create(Stopping.class, callbacks -> server -> {
		for (var callback : callbacks) {
			callback.stoppingServer(server);
		}
	});

	/**
	 * An event indicating that a Minecraft server has finished shutting down.
	 *
	 * <p>What occurs after this event will vary depending on whether the Minecraft server is a dedicated server or the
	 * integrated server of a Minecraft client:
	 *
	 * <ul>
	 * <li><b>integrated server:</b> the client will continue to tick after this event is executed. If the client is being
	 * shut down, then this event is called after events indicating the client is being shut down are called.
	 * <li><b>dedicated server:</b> this will be the last event called before the Java Virtual Machine terminates.
	 * </ul>
	 *
	 * <h2>What should mods do when this event is executed?</h2>
	 * <p>
	 * Mods should stop referencing this Minecraft server or else the dead server will continue to be tracked on the
	 * heap and will leak memory. Though this doesn't matter when the server is a dedicated server, it is good principle
	 * to clean up references you no longer need regardless.
	 */
	public static final Event<Stopped> STOPPED = Event.create(Stopped.class, callbacks -> server -> {
		for (var callback : callbacks) {
			callback.exitServer(server);
		}
	});

	private ServerLifecycleEvents() {
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #STARTING}.
	 *
	 * @see #STARTING
	 */
	@FunctionalInterface
	public interface Starting extends EventAwareListener {
		/**
		 * Called when a Minecraft server is starting.
		 *
		 * @param server the server which is starting
		 */
		void startingServer(MinecraftServer server);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #READY}.
	 *
	 * @see #READY
	 */
	@FunctionalInterface
	public interface Ready extends EventAwareListener {
		/**
		 * Called when a Minecraft server is ready to tick and accept players.
		 *
		 * @param server the server which is ready
		 */
		void readyServer(MinecraftServer server);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #STOPPING}.
	 *
	 * @see #STOPPING
	 */
	@FunctionalInterface
	public interface Stopping extends EventAwareListener {
		/**
		 * Called when a Minecraft server has finished its last tick and is shutting down.
		 *
		 * @param server the server which is shutting down
		 */
		void stoppingServer(MinecraftServer server);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #STOPPED}.
	 *
	 * @see #STOPPED
	 */
	@FunctionalInterface
	public interface Stopped extends EventAwareListener {
		/**
		 * Called when a Minecraft server has finished shutdown and the server will be exited.
		 *
		 * @param server the minecraft server which is exiting
		 */
		void exitServer(MinecraftServer server);
	}
}
