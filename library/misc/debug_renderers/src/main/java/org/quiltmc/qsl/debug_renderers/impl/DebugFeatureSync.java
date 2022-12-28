package org.quiltmc.qsl.debug_renderers.impl;

import static org.quiltmc.qsl.debug_renderers.impl.Initializer.id;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.debug_renderers.api.DebugFeature;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

@ApiStatus.Internal
public final class DebugFeatureSync {
	public static final Identifier SYNC_MESSAGE_ID = id("feature_sync");

	@ClientOnly
	public static void syncFeaturesToServer() {
		var features = DebugFeaturesImpl.getFeatures();
		ClientPlayNetworking.send(SYNC_MESSAGE_ID, writeStatuses(features));
	}

	@ClientOnly
	public static void syncFeaturesToServer(DebugFeature... features) {
		ClientPlayNetworking.send(SYNC_MESSAGE_ID, writeStatuses(List.of(features)));
	}

	public static void syncFeaturesToClient(ServerPlayerEntity... players) {
		var features = DebugFeaturesImpl.getEnabledFeatures();
		ServerPlayNetworking.send(List.of(players), SYNC_MESSAGE_ID, writeStatuses(features));
	}

	public static void syncFeaturesToClient(Collection<ServerPlayerEntity> players, DebugFeature... features) {
		ServerPlayNetworking.send(players, SYNC_MESSAGE_ID, writeStatuses(List.of(features)));
	}

	public static PacketByteBuf writeStatuses(Collection<DebugFeature> features) {
		var buf = PacketByteBufs.create();
		buf.writeVarInt(features.size());
		for (var feature : features) {
			buf.writeIdentifier(feature.id());
			buf.writeBoolean(DebugFeaturesImpl.isEnabled(feature));
		}
		return buf;
	}

	public static Map<DebugFeature, Boolean> readStatuses(PacketByteBuf buf) {
		final int size = buf.readVarInt();
		var statuses = new HashMap<DebugFeature, Boolean>();
		for (int i = 0; i < size; i++) {
			var featureId = buf.readIdentifier();
			var feature = DebugFeaturesImpl.get(featureId);
			if (feature == null) {
				Initializer.LOGGER.warn("Received value for unknown debug feature {}", featureId);
				continue;
			}
			boolean enabled = buf.readBoolean();
			statuses.put(feature, enabled);
		}

		return statuses;
	}

	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(SYNC_MESSAGE_ID, (server, player, handler, buf, responseSender) -> {
			var statuses = readStatuses(buf);
			server.execute(() -> {
				DebugFeaturesImpl.setEnabledForPlayer(player, statuses);
			});
		});
	}

	@ClientOnly
	public static void clientInit() {
		ClientPlayNetworking.registerGlobalReceiver(SYNC_MESSAGE_ID, (client, handler, buf, responseSender) -> {
			var statuses = readStatuses(buf);
			client.execute(() -> {
				DebugFeaturesImpl.setEnabledOnServer(statuses);
				DebugFeatureSync.syncFeaturesToServer();
			});
		});
	}
}
