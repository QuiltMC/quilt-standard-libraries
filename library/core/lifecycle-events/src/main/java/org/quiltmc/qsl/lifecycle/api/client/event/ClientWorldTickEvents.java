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

package org.quiltmc.qsl.lifecycle.api.client.event;

import org.quiltmc.qsl.base.api.event.ArrayEvent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Events related to a ticking Minecraft client's world.
 *
 * <h2>A note of warning</h2>
 *
 * Callbacks registered to any of these events should ensure as little time as possible is spent executing, since the tick
 * loop is a very hot code path.
 */
@Environment(EnvType.CLIENT)
public final class ClientWorldTickEvents {
	/**
	 * An event indicating that a world will be ticked.
	 */
	public static final ArrayEvent<Start> START = ArrayEvent.create(Start.class, callbacks -> (client, world) -> {
		for (var callback : callbacks) {
			callback.startWorldTick(client, world);
		}
	});

	/**
	 * An event indicating that a world has finished being ticked.
	 */
	public static final ArrayEvent<End> END = ArrayEvent.create(End.class, callbacks -> (client, world) -> {
		for (var callback : callbacks) {
			callback.endWorldTick(client, world);
		}
	});

	private ClientWorldTickEvents() {}

	/**
	 * Functional interface to be implemented on callbacks for {@link #START}.
	 * @see #START
	 */
	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface Start {
		/**
		 * Called before a world is ticked.
		 *
		 * @param client the client
		 * @param world the world being ticked
		 */
		void startWorldTick(MinecraftClient client, ClientWorld world);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #END}.
	 * @see #END
	 */
	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface End {
		/**
		 * Called after a world is ticked.
		 *
		 * @param client the client
		 * @param world the world being ticked
		 */
		void endWorldTick(MinecraftClient client, ClientWorld world);
	}
}
