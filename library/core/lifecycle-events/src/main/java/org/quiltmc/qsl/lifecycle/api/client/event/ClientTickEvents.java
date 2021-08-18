package org.quiltmc.qsl.lifecycle.api.client.event;

import org.quiltmc.qsl.base.api.event.ArrayEvent;

import net.minecraft.client.MinecraftClient;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Events indicating progress through the tick loop of a Minecraft client.
 *
 * <h2>A note of warning</h2>
 *
 * Callbacks registered to any of these events should ensure as little time as possible is spent executing, since the tick
 * loop is a very hot code path.
 */
@Environment(EnvType.CLIENT)
public final class ClientTickEvents {
	/**
	 * An event indicating an iteration of the client's tick loop will start.
	 */
	public static final ArrayEvent<Start> START = ArrayEvent.create(Start.class, callbacks -> client -> {
		for (var callback : callbacks) {
			callback.startClientTick(client);
		}
	});

	/**
	 * An event indicating the client has finished an iteration of the tick loop.
	 *
	 * <p>Since there will be a time gap before the next tick, this is a great spot to run any asynchronous operations
	 * for the next tick.
	 */
	public static final ArrayEvent<End> END = ArrayEvent.create(End.class, callbacks -> client -> {
		for (var callback : callbacks) {
			callback.endClientTick(client);
		}
	});

	private ClientTickEvents() {}

	/**
	 * Functional interface to be implemented on callbacks for {@link #START}.
	 * @see #START
	 */
	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface Start {
		void startClientTick(MinecraftClient client);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #END}.
	 * @see #END
	 */
	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface End {
		void endClientTick(MinecraftClient client);
	}
}
