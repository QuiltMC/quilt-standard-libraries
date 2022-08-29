/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.registry.impl.sync;

import java.util.ArrayList;
import java.util.List;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.ButtonClickC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.CraftRequestC2SPacket;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.JigsawGeneratingC2SPacket;
import net.minecraft.network.packet.c2s.play.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PickFromInventoryC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayPongC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.QueryBlockNbtC2SPacket;
import net.minecraft.network.packet.c2s.play.QueryEntityNbtC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeCategoryOptionsC2SPacket;
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket;
import net.minecraft.network.packet.c2s.play.RequestChatPreviewC2SPacket;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.SelectMerchantTradeC2SPacket;
import net.minecraft.network.packet.c2s.play.SpectatorTeleportC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateBeaconC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateCommandBlockMinecartC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateDifficultyLockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateJigsawC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdatePlayerAbilitiesC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSignC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateStructureBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayPingS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.unmapped.C_vtnjglse;

import org.quiltmc.qsl.networking.impl.ChannelInfoHolder;

/**
 * All the magic happens here!
 * <p>
 * This is special PacketListener for handling registry sync.
 * Why does it exist? Wouldn't usage of login packets be better?
 * <p>
 * And well, yes it would, but sadly these aren't compatible with proxy
 * software like Velocity (see Forge). Thankfully emulating them on PLAY
 * protocol isn't too hard and gives equal results. And doing them on PLAY
 * is needed for Fabric compatibility anyway
 */
@ApiStatus.Internal
public final class ServerRegistrySyncNetworkHandler implements ServerPlayPacketListener {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final int HELLO_PING = 0;
	private static final int GOODBYE_PING = 1;

	private final ClientConnection connection;
	private final ServerPlayerEntity player;
	private final Runnable continueLoginRunnable;

	private final List<CustomPayloadC2SPacket> delayedPackets = new ArrayList<>();
	private int syncVersion = -1;

	public ServerRegistrySyncNetworkHandler(ServerPlayerEntity player, ClientConnection connection, Runnable continueLogin) {
		this.connection = connection;
		this.player = player;
		this.continueLoginRunnable = continueLogin;

		((DelayedPacketsHolder) this.player).quilt$setPacketList(this.delayedPackets);

		ServerRegistrySync.sendHelloPacket(connection);
		connection.send(new PlayPingS2CPacket(HELLO_PING));
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onPong(PlayPongC2SPacket packet) {
		switch (packet.getParameter()) {
			case HELLO_PING -> {
				if (this.syncVersion != -1) {
					ServerRegistrySync.sendSyncPackets(this.connection, this.player);
				} else if (ServerRegistrySync.supportFabric && ((ChannelInfoHolder) this.connection).getPendingChannelsNames().contains(ServerFabricRegistrySync.ID)) {
					ServerFabricRegistrySync.sendSyncPackets(this.connection);
					this.syncVersion = -2;
				}
				connection.send(new PlayPingS2CPacket(GOODBYE_PING));
			}
			case GOODBYE_PING -> {
				if (this.syncVersion == -1 && ServerRegistrySync.requiresSync()) {
					this.disconnect(ServerRegistrySync.noRegistrySyncMessage);
				} else {
					this.continueLogin();
				}
			}
		}
	}

	private void continueLogin() {
		this.player.server.execute(this.continueLoginRunnable);
	}

	@Override
	public void onCustomPayload(CustomPayloadC2SPacket packet) {
		if (packet.getChannel().equals(ClientPackets.HANDSHAKE)) {
			this.syncVersion = packet.getData().readVarInt();
		} else if (packet.getChannel().equals(ClientPackets.SYNC_FAILED)) {
			LOGGER.info("Disconnecting {} due to sync failure of {} registry", player.getGameProfile().getName(), packet.getData().readIdentifier());
		} else {
			this.delayedPackets.add(new CustomPayloadC2SPacket(packet.getChannel(), new PacketByteBuf(packet.getData().copy())));
		}
	}


	@Override
	public void onDisconnected(Text reason) {
		LOGGER.info("{} lost connection: {}", this.player.getName().getString(), reason.getString());

		for (var packet : this.delayedPackets) {
			packet.getData().release();
		}
	}

	public void disconnect(Text reason) {
		try {
			for (var packet : this.delayedPackets) {
				packet.getData().release();
			}
			this.connection.send(new DisconnectS2CPacket(reason), PacketSendListener.alwaysRun(() -> {
				this.connection.disconnect(reason);
			}));
		} catch (Exception var3) {
			LOGGER.error("Error whilst disconnecting player", var3);
		}
	}

	@Override
	public ClientConnection getConnection() {
		return this.connection;
	}

	@Override
	public void onHandSwing(HandSwingC2SPacket packet) {}

	@Override
	public void onChatMessage(ChatMessageC2SPacket packet) {}

	@Override
	public void onChatCommand(ChatCommandC2SPacket packet) {}

	@Override
	public void onRequestChatPreview(RequestChatPreviewC2SPacket packet) {}

	@Override
	public void method_44898(C_vtnjglse c_vtnjglse) {}

	@Override
	public void onClientStatus(ClientStatusC2SPacket packet) {}

	@Override
	public void onClientSettings(ClientSettingsC2SPacket packet) {}

	@Override
	public void onButtonClick(ButtonClickC2SPacket packet) {}

	@Override
	public void onClickSlot(ClickSlotC2SPacket packet) {}

	@Override
	public void onCraftRequest(CraftRequestC2SPacket packet) {}

	@Override
	public void onCloseHandledScreen(CloseHandledScreenC2SPacket packet) {}

	@Override
	public void onPlayerInteractEntity(PlayerInteractEntityC2SPacket packet) {}

	@Override
	public void onKeepAlive(KeepAliveC2SPacket packet) {}

	@Override
	public void onPlayerMove(PlayerMoveC2SPacket packet) {}

	@Override
	public void onPlayerAbilities(UpdatePlayerAbilitiesC2SPacket packet) {}

	@Override
	public void onPlayerAction(PlayerActionC2SPacket packet) {}

	@Override
	public void onClientCommand(ClientCommandC2SPacket packet) {}

	@Override
	public void onPlayerInput(PlayerInputC2SPacket packet) {}

	@Override
	public void onUpdateSelectedSlot(UpdateSelectedSlotC2SPacket packet) {}

	@Override
	public void onCreativeInventoryAction(CreativeInventoryActionC2SPacket packet) {}

	@Override
	public void onSignUpdate(UpdateSignC2SPacket packet) {}

	@Override
	public void onPlayerInteractBlock(PlayerInteractBlockC2SPacket packet) {}

	@Override
	public void onPlayerInteractItem(PlayerInteractItemC2SPacket packet) {}

	@Override
	public void onSpectatorTeleport(SpectatorTeleportC2SPacket packet) {}

	@Override
	public void onResourcePackStatus(ResourcePackStatusC2SPacket packet) {}

	@Override
	public void onBoatPaddleState(BoatPaddleStateC2SPacket packet) {}

	@Override
	public void onVehicleMove(VehicleMoveC2SPacket packet) {}

	@Override
	public void onTeleportConfirm(TeleportConfirmC2SPacket packet) {}

	@Override
	public void onRecipeBookData(RecipeBookDataC2SPacket packet) {}

	@Override
	public void onRecipeCategoryOptions(RecipeCategoryOptionsC2SPacket packet) {}

	@Override
	public void onAdvancementTab(AdvancementTabC2SPacket packet) {}

	@Override
	public void onRequestCommandCompletions(RequestCommandCompletionsC2SPacket packet) {}

	@Override
	public void onUpdateCommandBlock(UpdateCommandBlockC2SPacket packet) {}

	@Override
	public void onUpdateCommandBlockMinecart(UpdateCommandBlockMinecartC2SPacket packet) {}

	@Override
	public void onPickFromInventory(PickFromInventoryC2SPacket packet) {}

	@Override
	public void onRenameItem(RenameItemC2SPacket packet) {}

	@Override
	public void onUpdateBeacon(UpdateBeaconC2SPacket packet) {}

	@Override
	public void onStructureBlockUpdate(UpdateStructureBlockC2SPacket packet) {}

	@Override
	public void onMerchantTradeSelect(SelectMerchantTradeC2SPacket packet) {}

	@Override
	public void onBookUpdate(BookUpdateC2SPacket packet) {}

	@Override
	public void onQueryEntityNbt(QueryEntityNbtC2SPacket packet) {}

	@Override
	public void onQueryBlockNbt(QueryBlockNbtC2SPacket packet) {}

	@Override
	public void onJigsawUpdate(UpdateJigsawC2SPacket packet) {}

	@Override
	public void onJigsawGenerating(JigsawGeneratingC2SPacket packet) {}

	@Override
	public void onUpdateDifficulty(UpdateDifficultyC2SPacket packet) {}

	@Override
	public void onUpdateDifficultyLock(UpdateDifficultyLockC2SPacket packet) {}
}
