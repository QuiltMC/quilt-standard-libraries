/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.registry.impl.sync.server;

import java.util.ArrayList;
import java.util.List;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.impl.payload.PacketByteBufPayload;
import org.slf4j.Logger;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.listener.ServerConfigurationPacketListener;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.MessageAcknowledgmentC2SPacket;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.common.KeepConnectionAliveC2SPacket;
import net.minecraft.network.packet.c2s.common.PongC2SPacket;
import net.minecraft.network.packet.c2s.common.ResourcePackStatusUpdateC2SPacket;
import net.minecraft.network.packet.c2s.configuration.FinishConfigurationC2SPacket;
import net.minecraft.network.packet.c2s.play.AdvancementTabOpenC2SPacket;
import net.minecraft.network.packet.c2s.play.BeaconUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.BlockNbtQueryC2SPacket;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatSessionUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.ChunkBatchAcknowledgementC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientSettingsUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandBlockMinecartUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandBlockUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandCompletionRequestC2SPacket;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.DifficultyLockUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.DifficultyUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.EntityNbtQueryC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.HandledScreenCloseC2SPacket;
import net.minecraft.network.packet.c2s.play.InventoryItemPickC2SPacket;
import net.minecraft.network.packet.c2s.play.ItemRenameC2SPacket;
import net.minecraft.network.packet.c2s.play.JigsawGenerationC2SPacket;
import net.minecraft.network.packet.c2s.play.JigsawUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.MerchantTradeSelectionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerAbilityUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractionWithBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractionWithEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractionWithItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeBookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeCategoryOptionUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.ReconfigurationAcknowledgementC2SPacket;
import net.minecraft.network.packet.c2s.play.SelectedSlotUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.SignUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.SlotClickC2SPacket;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportationC2SPacket;
import net.minecraft.network.packet.c2s.play.StructureBlockUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmationC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.c2s.query.QueryPingC2SPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.common.PingS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.impl.ChannelInfoHolder;
import org.quiltmc.qsl.registry.impl.sync.ClientPackets;
import org.quiltmc.qsl.registry.impl.sync.ProtocolVersions;

/**
 * All the magic happens here!
 * <p>
 * This is special PacketListener for handling registry sync.
 * Why does it exist? Wouldn't usage of login packets be better?
 * <p>
 * And well, yes it would, but sadly these can't be made compatible with proxy
 * software like Velocity (see Forge). Thankfully emulating them on PLAY
 * protocol isn't too hard and gives equal results. And doing them on PLAY
 * is needed for Fabric compatibility anyway.
 * It still doesn't work with Velocity out of the box (they don't care much about this
 * being valid), getting support is still simple.
 */
@ApiStatus.Internal
public final class ServerRegistrySyncNetworkHandler implements ServerConfigurationPacketListener {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final int HELLO_PING = 0;
	private static final int GOODBYE_PING = 1;

	private final GameProfile profile;
	private final MinecraftServer server;
	private final ClientConnection connection;
	private final ExtendedConnectionClient extendedConnection;
	private final Runnable continueLoginRunnable;

	private final List<CustomPayloadC2SPacket> delayedPackets = new ArrayList<>();
	private int syncVersion = ProtocolVersions.NO_PROTOCOL;

	public ServerRegistrySyncNetworkHandler(GameProfile profile, MinecraftServer server, ClientConnection connection, Runnable continueLogin) {
		this.profile = profile;
		this.server = server;
		this.connection = connection;
		this.continueLoginRunnable = continueLogin;
		this.extendedConnection = (ExtendedConnectionClient) connection;

		((DelayedPacketsHolder) this.connection).quilt$setPacketList(this.delayedPackets);

		ServerRegistrySync.sendHelloPacket(connection);
		connection.send(new PingS2CPacket(HELLO_PING));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onPlayPong(PongC2SPacket packet) {
		switch (packet.getParameter()) {
			case HELLO_PING -> {
				if (ServerRegistrySync.SERVER_SUPPORTED_PROTOCOL.contains(this.syncVersion)) {
					this.extendedConnection.quilt$setUnderstandsOptional();
					ServerRegistrySync.sendSyncPackets(this.connection, this.syncVersion);
				} else if (ServerRegistrySync.SERVER_SUPPORTED_PROTOCOL.contains(ProtocolVersions.FAPI_PROTOCOL) && (ServerRegistrySync.forceFabricFallback || (ServerRegistrySync.supportFabric && ((ChannelInfoHolder) this.connection).getPendingChannelsNames(NetworkState.PLAY).contains(ServerFabricRegistrySync.ID)))) {
					ServerFabricRegistrySync.sendSyncPackets(this.connection);
					this.syncVersion = ProtocolVersions.FAPI_PROTOCOL;
				}

				this.connection.send(new PingS2CPacket(GOODBYE_PING));
			}
			case GOODBYE_PING -> {
				if (this.syncVersion == ProtocolVersions.NO_PROTOCOL && ServerRegistrySync.requiresSync()) {
					this.disconnect(ServerRegistrySync.noRegistrySyncMessage);
				} else {
					this.continueLogin();
				}
			}
		}
	}

	private void continueLogin() {
		this.server.execute(this.continueLoginRunnable);
	}

	@Override
	public void onCustomPayload(CustomPayloadC2SPacket packet) {
		PacketByteBufPayload payload = (PacketByteBufPayload) packet.payload();
		Identifier id = payload.id();
		PacketByteBuf data = payload.data();
		if (id.equals(ClientPackets.HANDSHAKE)) {
			this.syncVersion = data.readVarInt();
		} else if (id.equals(ClientPackets.SYNC_FAILED)) {
			LOGGER.info("Disconnecting {} due to sync failure of {} registry", this.profile.getName(), data.readIdentifier());
		} else if (id.equals(ClientPackets.UNKNOWN_ENTRY)) {
			this.handleUnknownEntry(data);
		} else if (id.equals(ClientPackets.MOD_PROTOCOL)) {
			this.handleModProtocol(data);
		} else {
			this.delayedPackets.add(new CustomPayloadC2SPacket(new PacketByteBufPayload(id, PacketByteBufs.copy(data))));
		}
	}

	private void handleModProtocol(PacketByteBuf data) {
		var count = data.readVarInt();
		while (count-- > 0) {
			var id = data.readString();
			var version = data.readVarInt();
			this.extendedConnection.quilt$setModProtocol(id, version);
		}
	}

	private void handleUnknownEntry(PacketByteBuf data) {
		var registry = Registries.REGISTRY.get(data.readIdentifier());
		var length = data.readVarInt();

		while (length-- > 0) {
			var object = registry.get(data.readVarInt());

			if (object != null) {
				this.extendedConnection.quilt$addUnknownEntry(registry, object);
			}
		}
	}

	@Override
	public void onDisconnected(Text reason) {
		LOGGER.info("{} lost connection: {}", this.profile.getName(), reason.getString());

		for (var packet : this.delayedPackets) {
			PacketByteBuf data = ((PacketByteBufPayload) packet.payload()).data();
			if (data.refCnt() != 0) {
				data.release(data.refCnt());
			}
		}
	}

	public void disconnect(Text reason) {
		try {
			for (var packet : this.delayedPackets) {
				PacketByteBuf data = ((PacketByteBufPayload) packet.payload()).data();
				if (data.refCnt() != 0) {
					data.release(data.refCnt());
				}
			}

			this.connection.send(new DisconnectS2CPacket(reason),
					PacketSendListener.alwaysRun(() -> this.connection.disconnect(reason))
			);
		} catch (Exception var3) {
			LOGGER.error("Error whilst disconnecting player", var3);
		}
	}

	@Override
	public boolean isConnected() {
		return this.connection.isOpen();
	}


	@Override
	public void onKeepConnectionAlive(KeepConnectionAliveC2SPacket packet) {}


	@Override
	public void onResourcePackStatusUpdate(ResourcePackStatusUpdateC2SPacket packet) {}

	@Override
	public void method_12069(ClientSettingsUpdateC2SPacket clientSettingsUpdateC2SPacket) {}

	@Override
	public void onFinishConfiguration(FinishConfigurationC2SPacket packet) {}
}
