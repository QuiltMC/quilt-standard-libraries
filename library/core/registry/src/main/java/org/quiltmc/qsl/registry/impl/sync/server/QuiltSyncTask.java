package org.quiltmc.qsl.registry.impl.sync.server;

import java.util.function.Consumer;

import com.mojang.logging.LogUtils;
import org.quiltmc.qsl.networking.impl.ChannelInfoHolder;
import org.quiltmc.qsl.registry.impl.sync.ClientPackets;
import org.quiltmc.qsl.registry.impl.sync.ProtocolVersions;
import org.quiltmc.qsl.registry.impl.sync.ServerPackets;
import org.slf4j.Logger;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.ServerConfigurationPacketHandler;
import net.minecraft.network.configuration.ConfigurationTask;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;

public class QuiltSyncTask implements ConfigurationTask {
	public static final Type TYPE = new Type("qsl:registry_sync");
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ServerConfigurationPacketHandler packetHandler;
	private final ClientConnection connection;
	private final ExtendedConnectionClient extendedConnection;
	private Consumer<Packet<?>> sender;
	private int syncVersion = ProtocolVersions.NO_PROTOCOL;
	public QuiltSyncTask(ServerConfigurationPacketHandler packetHandler, ClientConnection connection) {
		this.packetHandler = packetHandler;
		this.connection = connection;
		this.extendedConnection = (ExtendedConnectionClient) connection;
	}

	@Override
	public void start(Consumer<Packet<?>> sender) {
		this.sender = sender;
		ServerRegistrySync.sendHelloPacket(sender);
	}

	@Override
	public Type getType() {
		return TYPE;
	}

	@SuppressWarnings("deprecation")
	private void sendSyncPackets(Consumer<Packet<?>> sender) {
		if (ServerRegistrySync.SERVER_SUPPORTED_PROTOCOL.contains(this.syncVersion)) {
			this.extendedConnection.quilt$setUnderstandsOptional();
			ServerRegistrySync.sendSyncPackets(sender, this.syncVersion);
		} else if (
			ServerRegistrySync.SERVER_SUPPORTED_PROTOCOL.contains(ProtocolVersions.FAPI_PROTOCOL)
			&& (ServerRegistrySync.forceFabricFallback
				|| (ServerRegistrySync.supportFabric
					&& ((ChannelInfoHolder) this.connection).getPendingChannelsNames(NetworkState.CONFIGURATION).contains(ServerFabricRegistrySync.ID)
				)
			)
		) {
//			ServerFabricRegistrySync.sendSyncPackets(sender);
			this.syncVersion = ProtocolVersions.FAPI_PROTOCOL;
//			state.set(SyncState.RECEIVED_SYNC); // TODO: Look at fabrics new sync
		}
	}

	public void handleHandshake(ClientPackets.Handshake handshake) {
		this.syncVersion = handshake.version();
		this.sendSyncPackets(sender);
	}

	public void handleSyncFailed(ClientPackets.SyncFailed syncFailed) {
		LOGGER.info("Disconnecting {} due to sync failure of {} registry", this.packetHandler.getHost().getName(), syncFailed.registry());
	}

	public void handleModProtocol(ClientPackets.ModProtocol modProtocol) {
		modProtocol.protocols().forEach(this.extendedConnection::quilt$setModProtocol);
	}

	public void handleUnknownEntry(ClientPackets.UnknownEntry unknownEntry) {
		var registry = Registries.REGISTRY.get(unknownEntry.registry());

		unknownEntry.rawIds().forEach(id -> {
			var object = registry.get(id);

			if (object != null) {
				this.extendedConnection.quilt$addUnknownEntry(registry, object);
			}
		});
	}

	public void handleEnd(ClientPackets.End end) {
		if (this.syncVersion == ProtocolVersions.NO_PROTOCOL && ServerRegistrySync.requiresSync()) {
			this.packetHandler.disconnect(ServerRegistrySync.noRegistrySyncMessage);
		} else {
			((QuiltSyncTaskHolder) this.packetHandler).qsl$finishSyncTask();
		}
	}
}
