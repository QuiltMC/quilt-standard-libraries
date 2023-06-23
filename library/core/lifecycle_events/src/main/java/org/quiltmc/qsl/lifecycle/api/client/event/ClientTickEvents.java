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
 * Events indicating progress through the tick loop of a Minecraft client.
 *
 * <h2>A note of warning</h2>
 * <p>
 * Callbacks registered to any of these events should ensure as little time as possible is spent executing, since the tick
 * loop is a very hot code path.
 */
@ClientOnly
public final class ClientTickEvents {
	/**
	 * An event indicating an iteration of the client's tick loop will start.
	 */
	public static final Event<Start> START = Event.create(Start.class, callbacks -> client -> {
		for (var callback : callbacks) {
			callback.startClientTick(client);
		}
	});

	/**
	 * An event indicating the client has finished an iteration of the tick loop.
	 * <p>
	 * Since there will be a time gap before the next tick, this is a great spot to run any asynchronous operations
	 * for the next tick.
	 */
	public static final Event<End> END = Event.create(End.class, callbacks -> client -> {
		for (var callback : callbacks) {
			callback.endClientTick(client);
		}
	});

	private ClientTickEvents() {
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #START}.
	 *
	 * @see #START
	 */
	@FunctionalInterface
	@ClientOnly
	public interface Start extends ClientEventAwareListener {
		/**
		 * Called before the client has started an iteration of the tick loop.
		 *
		 * @param client the client
		 */
		void startClientTick(MinecraftClient client);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #END}.
	 *
	 * @see #END
	 */
	@FunctionalInterface
	@ClientOnly
	public interface End extends ClientEventAwareListener {
		/**
		 * Called at the end of an iteration of the client's tick loop.
		 *
		 * @param client the client that finished ticking
		 */
		void endClientTick(MinecraftClient client);
	}
}
