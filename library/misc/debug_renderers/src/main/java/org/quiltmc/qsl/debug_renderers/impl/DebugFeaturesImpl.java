package org.quiltmc.qsl.debug_renderers.impl;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.debug_renderers.api.DebugFeature;

import java.util.*;

@ApiStatus.Internal
public class DebugFeaturesImpl {
	private static final Map<Identifier, DebugFeature> DEBUG_FEATURES = new HashMap<>();
	private static final Set<Identifier> ENABLED_FEATURES = new HashSet<>();
	private static final WeakHashMap<ServerPlayerEntity, Set<Identifier>> ENABLED_FEATURES_PER_PLAYER = new WeakHashMap<>();
	@ClientOnly
	private static final Set<Identifier> ENABLED_FEATURES_ON_SERVER = new HashSet<>();

	public static DebugFeature register(DebugFeature feature) {
		DEBUG_FEATURES.put(feature.id(), feature);
		return feature;
	}

	public static @Nullable DebugFeature get(Identifier id) {
		return DEBUG_FEATURES.get(id);
	}

	public static Set<DebugFeature> getFeatures() {
		return new HashSet<>(DEBUG_FEATURES.values());
	}

	public static boolean isEnabled(DebugFeature feature) {
		return ENABLED_FEATURES.contains(feature.id());
	}

	public static void setEnabled(DebugFeature feature, boolean value) {
		if (value) {
			ENABLED_FEATURES.add(feature.id());
		} else {
			ENABLED_FEATURES.remove(feature.id());
		}
	}

	public static boolean isEnabledForPlayer(ServerPlayerEntity player, DebugFeature feature) {
		return ENABLED_FEATURES_PER_PLAYER.getOrDefault(player, Set.of()).contains(feature.id());
	}

	public static void setEnabledForPlayer(ServerPlayerEntity player, DebugFeature feature, boolean value) {
		var set = ENABLED_FEATURES_PER_PLAYER.getOrDefault(player, new HashSet<>());
		if (value) {
			set.add(feature.id());
		} else {
			set.remove(feature.id());
		}
		ENABLED_FEATURES_PER_PLAYER.put(player, set);
	}

	public static void setEnabledForPlayer(ServerPlayerEntity player, Map<DebugFeature, Boolean> statuses) {
		var set = ENABLED_FEATURES_PER_PLAYER.getOrDefault(player, new HashSet<>());
		for (var entry : statuses.entrySet()) {
			if (entry.getValue()) {
				set.add(entry.getKey().id());
			} else {
				set.remove(entry.getKey().id());
			}
		}
		ENABLED_FEATURES_PER_PLAYER.put(player, set);
	}

	@ClientOnly
	public static boolean isEnabledOnServer(DebugFeature feature) {
		return ENABLED_FEATURES_ON_SERVER.contains(feature.id());
	}

	@ClientOnly
	public static void setEnabledOnServer(DebugFeature feature, boolean value) {
		if (value) {
			ENABLED_FEATURES_ON_SERVER.add(feature.id());
		} else {
			ENABLED_FEATURES_ON_SERVER.remove(feature.id());
		}
	}

	@ClientOnly
	public static void setEnabledOnServer(Map<DebugFeature, Boolean> statuses) {
		for (var entry : statuses.entrySet()) {
			if (entry.getValue()) {
				ENABLED_FEATURES_ON_SERVER.add(entry.getKey().id());
			} else {
				ENABLED_FEATURES_ON_SERVER.remove(entry.getKey().id());
			}
		}
	}
}
