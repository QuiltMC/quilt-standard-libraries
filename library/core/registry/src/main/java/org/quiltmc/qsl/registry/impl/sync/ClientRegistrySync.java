package org.quiltmc.qsl.registry.impl.sync;

import com.mojang.logging.LogUtils;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.registry.api.sync.RegistryFlag;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class ClientRegistrySync {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static SynchronizedRegistry<?> currentRegistry;
	private static Map<String, Collection<SynchronizedRegistry.SyncEntry>> syncMap;
	private static int currentCount;
	private static byte currentFlags;
	private static boolean optionalRegistry;

	public static void registerHandlers() {
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.START, ClientRegistrySync::handleStartPacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.DATA, ClientRegistrySync::handleDataPacket);
		ClientPlayNetworking.registerGlobalReceiver(ServerPackets.APPLY, ClientRegistrySync::handleApplyPacket);
	}

	private static void handleStartPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		var identifier = buf.readIdentifier();
		var count = buf.readVarInt();
		var flags = buf.readByte();

		var registry = Registry.REGISTRIES.get(identifier);

		if (registry instanceof SynchronizedRegistry synchronizedRegistry) {
			currentRegistry = synchronizedRegistry;
			currentCount = count;
			currentFlags = flags;
			syncMap = new HashMap<>();
		} else if (RegistryFlag.isOptional(flags)) {
			optionalRegistry = true;
		} else {
			LOGGER.warn("Trying to sync registry " + identifier + " which doesn't " + (registry == null ? "support it!" : "exist!"));
			handler.getConnection().disconnect(new LiteralText("Client is missing required registry! Mismatched mods?"));

		}
	}

	private static void handleDataPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		if (currentRegistry == null || syncMap == null) {
			if (!optionalRegistry) {
				LOGGER.warn("Received sync data without specifying registry!");
			}
			return;
		}

		var countNamespace = buf.readVarInt();
		while (countNamespace-- > 0) {
			var namespace = buf.readString();
			var countLocal = buf.readVarInt();

			while (countLocal-- > 0) {
				var path = buf.readString();
				var id = buf.readVarInt();
				var flags = buf.readByte();

				syncMap.computeIfAbsent(namespace, n -> new ArrayList<>()).add(new SynchronizedRegistry.SyncEntry(path, id, flags));
			}
		}
	}

	private static void handleApplyPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		if (currentRegistry == null || syncMap == null) {
			if (!optionalRegistry) {
				LOGGER.warn("Received sync data without specifying registry!");
			}
			return;
		}

		var reg = currentRegistry;
		var missingEntries = currentRegistry.quilt$applySyncMap(syncMap);

		boolean disconnect = false;

		if (!optionalRegistry) {
			for (var entry : missingEntries) {
				if (!RegistryFlag.isOptional(entry.flags())) {
					handler.getConnection().disconnect(new LiteralText("Client registry is missing entries! Mismatched mods?"));
				}
			}
		}


		if (!disconnect && reg == Registry.BLOCK) {
			rebuildStates();
		}

		currentRegistry = null;
		currentCount = 0;
		currentFlags = (byte) 0;
		optionalRegistry = false;

		syncMap = null;
	}

	public static void rebuildStates() {
		SynchronizedIdList.clear(Block.STATE_IDS);

		for (var block : Registry.BLOCK) {
			block.getStateManager().getStates().forEach(Block.STATE_IDS::add);
		}
	}
}
