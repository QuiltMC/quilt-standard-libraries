package org.quiltmc.qsl.lifecycle.api.client.event;

import org.quiltmc.qsl.base.api.event.ArrayEvent;

import net.minecraft.client.MinecraftClient;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Events indicating the lifecycle of a Minecraft client.
 *
 * <p>The lifecycle of a Minecraft client, starts when the client is ready. The client will tick the client and then the
 * integrated server if in single player.
 *
 * @see ClientTickEvents
 */
@Environment(EnvType.CLIENT)
public final class ClientLifecycleEvents {
	// There is no CLIENT_STARTING event because there is no way to allow mods to register callbacks that early without
	// possibly initializing the game's registries improperly in preLaunch.

	/**
	 * An event indicating that a Minecraft client is ready to tick and render.
	 *
	 * <p>It should be noted this event is executed while the splash screen is visible, not the main menu.
	 */
	public static final ArrayEvent<Ready> READY = ArrayEvent.create(Ready.class, callbacks -> client -> {
		for (var callback : callbacks) {
			callback.readyClient(client);
		}
	});

	/**
	 * An event indicating that a Minecraft client has finished it's last tick and will shut down.
	 *
	 * <p>If the client is connected to a server, the client will disconnect from the server. Then if the client has a
	 * running an integrated server, the integrated server will be shutdown. Finally all client facilities are torn down.
	 *
	 * <h2>What should mods do when this event is executed</h2>
	 *
	 * Mods which maintain session data when connected to a server should should save that data here as those mods still
	 * have access to the connected server.
	 *
	 * <p>If your mod has any data on the integrated server, avoid doing that here, use
	 * {@link org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents#STOPPING ServerLifecycleEvents.SERVER_STOPPING}
	 * instead to clean up any data on the integrated server.
	 */
	public static final ArrayEvent<Stopping> STOPPING = ArrayEvent.create(Stopping.class, callbacks -> client -> {
		for (var callback : callbacks) {
			callback.stoppingClient(client);
		}
	});

	/**
	 * An event indicating the client has finished shutdown and will exit.
	 *
	 * <p>The Java Virtual Machine will terminate after this event is executed.
	 */
	public static final ArrayEvent<Stopped> STOPPED = ArrayEvent.create(Stopped.class, callbacks -> client -> {
		for (var callback : callbacks) {
			callback.stoppedClient(client);
		}
	});

	private ClientLifecycleEvents() {}

	/**
	 * Functional interface to be implemented on callbacks for {@link #READY}.
	 * @see #READY
	 */
	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface Ready {
		void readyClient(MinecraftClient client);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #STOPPING}.
	 * @see #STOPPING
	 */
	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface Stopping {
		void stoppingClient(MinecraftClient client);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #STOPPED}.
	 * @see #STOPPED
	 */
	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface Stopped {
		void stoppedClient(MinecraftClient client);
	}
}
