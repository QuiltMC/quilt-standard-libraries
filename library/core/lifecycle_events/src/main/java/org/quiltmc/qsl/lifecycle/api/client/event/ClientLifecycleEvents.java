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

package org.quiltmc.qsl.lifecycle.api.client.event;

import net.minecraft.client.MinecraftClient;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.client.ClientEventAwareListener;

/**
 * Events indicating the lifecycle of a Minecraft client.
 * <p>
 * The lifecycle of a Minecraft client, starts when the client is ready. The client will tick the client and then the
 * integrated server if in single player.
 */
@ClientOnly
public final class ClientLifecycleEvents {
	// There is no STARTING event because there is no way to allow mods to register callbacks that early without possibly
	// initializing the game's registries improperly in preLaunch.

	/**
	 * An event indicating that a Minecraft client is ready to tick and render.
	 * <p>
	 * It should be noted this event is executed while the splash screen is visible, not the main menu.
	 */
	public static final Event<Ready> READY = Event.create(Ready.class, callbacks -> client -> {
		for (var callback : callbacks) {
			callback.readyClient(client);
		}
	});

	/**
	 * An event indicating that a Minecraft client has finished its last tick and will shut down.
	 * <p>
	 * After this event is fired, the client will disconnect from the server if it is connected to one. Then, if the client
	 * was running an integrated server, the integrated server will be shut down. Finally, all client facilities are torn down.
	 *
	 * <h2>What should mods do when this event is executed?</h2>
	 * <p>
	 * Mods which maintain session data when connected to a server should save that data here, as the client will still
	 * have access to the connected server.
	 * <p>
	 * If your mod has any data on the integrated server, avoid doing that here, use
	 * {@link org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents#STOPPING ServerLifecycleEvents.STOPPING}
	 * instead to clean up any data on the integrated server.
	 */
	public static final Event<Stopping> STOPPING = Event.create(Stopping.class, callbacks -> client -> {
		for (var callback : callbacks) {
			callback.stoppingClient(client);
		}
	});

	/**
	 * An event indicating the client has finished shutdown and will exit.
	 * <p>
	 * The Java Virtual Machine will terminate after this event is executed.
	 */
	public static final Event<Stopped> STOPPED = Event.create(Stopped.class, callbacks -> client -> {
		for (var callback : callbacks) {
			callback.stoppedClient(client);
		}
	});

	private ClientLifecycleEvents() {
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #READY}.
	 *
	 * @see #READY
	 */
	@FunctionalInterface
	@ClientOnly
	public interface Ready extends ClientEventAwareListener {
		/**
		 * Called when a majority of client facilities have been initialized.
		 * <p>
		 * It should be noted this is executed while the splash screen is visible, not when the main menu is reached.
		 *
		 * @param client the client which is read.
		 */
		void readyClient(MinecraftClient client);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #STOPPING}.
	 *
	 * @see #STOPPING
	 */
	@FunctionalInterface
	@ClientOnly
	public interface Stopping extends ClientEventAwareListener {
		/**
		 * Called when a Minecraft client has finished its last tick and is shutting down.
		 *
		 * @param client the client which is shutting down
		 */
		void stoppingClient(MinecraftClient client);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #STOPPED}.
	 *
	 * @see #STOPPED
	 */
	@FunctionalInterface
	@ClientOnly
	public interface Stopped extends ClientEventAwareListener {
		/**
		 * Called when a Minecraft client has finished shutdown and the client will be exited.
		 * <p>
		 * This is typically executed just before the Java virtual machine is shut down.
		 *
		 * @param client the minecraft client which is exiting
		 */
		void stoppedClient(MinecraftClient client);
	}
}
