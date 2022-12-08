package org.quiltmc.qsl.debug_renderers.api;

import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.debug_renderers.impl.DebugFeaturesImpl;

import java.util.Objects;

public final class DebugFeature {
	private final Identifier id;
	private final boolean needsServer;

	private DebugFeature(Identifier id, boolean needsServer) {
		this.id = id;
		this.needsServer = needsServer;
	}

	public boolean isEnabled() {
		return DebugFeaturesImpl.isEnabled(this);
	}

	public boolean isEnabledOnServerAndClient(ServerPlayerEntity player) {
		return DebugFeaturesImpl.isEnabled(this) && DebugFeaturesImpl.isEnabledForPlayer(player, this);
	}

	@ClientOnly
	boolean isEnabledOnServerAndClient() {
		return DebugFeaturesImpl.isEnabledOnServer(this) && DebugFeaturesImpl.isEnabledOnServer(this);
	}

	@ClientOnly
	boolean shouldRender() {
		return this.isEnabled() && !this.needsServer() || this.isEnabledOnServerAndClient();
	}

	public static DebugFeature register(Identifier id, boolean needsServer) {
		var existingFeature = DebugFeaturesImpl.get(id);
		if (existingFeature != null) {
			throw new IllegalArgumentException("A debug feature with the id %s already exists!".formatted(id));
		}
		var newFeature = new DebugFeature(id, needsServer);
		return DebugFeaturesImpl.register(newFeature);
	}

	public Identifier id() {
		return this.id;
	}

	public boolean needsServer() {
		return this.needsServer;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj == null || obj.getClass() != this.getClass()) return false;
		var that = (DebugFeature) obj;
		return Objects.equals(this.id, that.id) &&
				this.needsServer == that.needsServer;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id, this.needsServer);
	}

	@Override
	public String toString() {
		return "DebugFeature[" +
				"id=" + this.id + ", " +
				"needsServer=" + this.needsServer + ']';
	}

}
