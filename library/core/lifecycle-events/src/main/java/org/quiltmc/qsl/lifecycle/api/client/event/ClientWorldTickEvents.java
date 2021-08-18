package org.quiltmc.qsl.lifecycle.api.client.event;

import org.quiltmc.qsl.base.api.event.ArrayEvent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

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
		void startWorldTick(MinecraftClient client, ClientWorld world);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #END}.
	 * @see #END
	 */
	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface End {
		void endWorldTick(MinecraftClient client, ClientWorld world);
	}
}
