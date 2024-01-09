/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2024 The Quilt Project
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

package org.quiltmc.qsl.networking.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.ServerConfigurationPacketHandler;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerConfigurationNetworking;
import org.quiltmc.qsl.networking.api.client.ClientConfigurationNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.networking.impl.ChannelInfoHolder;
import org.quiltmc.qsl.networking.impl.client.ClientConfigurationNetworkAddon;
import org.quiltmc.qsl.networking.impl.client.ClientNetworkingImpl;
import org.quiltmc.qsl.networking.impl.common.CommonPacketHandler;
import org.quiltmc.qsl.networking.impl.common.CommonPacketsImpl;
import org.quiltmc.qsl.networking.impl.common.CommonRegisterPayload;
import org.quiltmc.qsl.networking.impl.common.CommonVersionPayload;
import org.quiltmc.qsl.networking.impl.server.ServerConfigurationNetworkAddon;
import org.quiltmc.qsl.networking.impl.server.ServerNetworkingImpl;

@SuppressWarnings("unchecked")
public class CommonPacketTests {
	private PacketSender<CustomPayload> packetSender;
	private ChannelInfoHolder channelInfoHolder;

	private ClientConfigurationNetworkHandler clientNetworkHandler;
	private ClientConfigurationNetworkAddon clientAddon;

	private ServerConfigurationPacketHandler serverNetworkHandler;
	private ServerConfigurationNetworkAddon serverAddon;

	private static final Identifier CLIENT_RECEIVE = new Identifier("quilt", "global_client");
	private static final Identifier CLIENT_RECEIVE_CONFIGURATION = new Identifier("quilt", "global_configuration_client");
	private static final Identifier SERVER_RECEIVE = new Identifier("quilt", "test");

	@BeforeAll
	static void beforeAll() {
		CommonPacketsImpl.init(null);
		ClientNetworkingImpl.clientInit(null);

		// Register a receiver to send in the play registry response
		ClientPlayNetworking.registerGlobalReceiver(CLIENT_RECEIVE, (client, handler, buf, responseSender) -> {
		});
	}

	@BeforeEach
	void setUp() {
		packetSender = mock(PacketSender.class);
		channelInfoHolder = new MockChannelInfoHolder();

		clientNetworkHandler = mock(ClientConfigurationNetworkHandler.class);
		clientAddon = mock(ClientConfigurationNetworkAddon.class);
		when(ClientNetworkingImpl.getAddon(clientNetworkHandler)).thenReturn(clientAddon);
		when(clientAddon.getChannelInfoHolder()).thenReturn(channelInfoHolder);

		serverNetworkHandler = mock(ServerConfigurationPacketHandler.class);
		serverAddon = mock(ServerConfigurationNetworkAddon.class);
		when(ServerNetworkingImpl.getAddon(serverNetworkHandler)).thenReturn(serverAddon);
		when(serverAddon.getChannelInfoHolder()).thenReturn(channelInfoHolder);
	}

	// Test handling the version packet on the client
	@Test
	void handleVersionPacketClient() {
		var packetHandler = ((ClientConfigurationNetworking.CustomChannelReceiver<CommonVersionPayload>) ClientNetworkingImpl.CONFIGURATION.getReceiver(CommonVersionPayload.PACKET_ID));
		assertNotNull(packetHandler);

		// Receive a packet from the server
		CommonVersionPayload payload = new CommonVersionPayload(new int[]{1, 2, 3});

		packetHandler.receive(null, clientNetworkHandler, payload, packetSender);

		// Check the response we are sending back to the server
		PacketByteBuf response = readResponse(packetSender);
		assertArrayEquals(new int[]{1}, response.readIntArray());
		assertEquals(0, response.readableBytes());

		assertEquals(1, getNegotiatedVersion(clientAddon));
	}

	// Test handling the version packet on the client, when the server sends unsupported versions
	@Test
	void handleVersionPacketClientUnsupported() {
		var packetHandler = ((ClientConfigurationNetworking.CustomChannelReceiver<CommonVersionPayload>) ClientNetworkingImpl.CONFIGURATION.getReceiver(CommonVersionPayload.PACKET_ID));
		assertNotNull(packetHandler);

		// Receive a packet from the server
		CommonVersionPayload payload = new CommonVersionPayload(new int[]{2, 3}); // We only support version 1

		assertThrows(UnsupportedOperationException.class, () -> {
			packetHandler.receive(null, clientNetworkHandler, payload, packetSender);
		});
	}

	// Test handling the version packet on the server
	@Test
	void handleVersionPacketServer() {
		var packetHandler = ((ServerConfigurationNetworking.CustomChannelReceiver<CommonVersionPayload>) ServerNetworkingImpl.CONFIGURATION.getReceiver(CommonVersionPayload.PACKET_ID));
		assertNotNull(packetHandler);

		// Receive a packet from the client
		CommonVersionPayload payload = new CommonVersionPayload(new int[]{1, 2, 3});

		packetHandler.receive(null, serverNetworkHandler, payload, null);

		assertEquals(1, getNegotiatedVersion(serverAddon));
	}

	// Test handling the version packet on the server unsupported version
	@Test
	void handleVersionPacketServerUnsupported() {
		var packetHandler = ((ServerConfigurationNetworking.CustomChannelReceiver<CommonVersionPayload>) ServerNetworkingImpl.CONFIGURATION.getReceiver(CommonVersionPayload.PACKET_ID));
		assertNotNull(packetHandler);

		// Receive a packet from the client
		CommonVersionPayload payload = new CommonVersionPayload(new int[]{3}); // Server only supports version 1

		assertThrows(UnsupportedOperationException.class, () -> {
			packetHandler.receive(null, serverNetworkHandler, payload, packetSender);
		});
	}

	// Test handing the play registry packet on the client configuration handler
	@Test
	void handlePlayRegistryClient() {
		var packetHandler = ((ClientConfigurationNetworking.CustomChannelReceiver<CommonRegisterPayload>) ClientNetworkingImpl.CONFIGURATION.getReceiver(CommonRegisterPayload.PACKET_ID));
		assertNotNull(packetHandler);

		when(clientAddon.getNegotiatedVersion()).thenReturn(1);

		// Receive a packet from the server
		CommonRegisterPayload payload = new CommonRegisterPayload(
			1,
			"play",
			Set.of(SERVER_RECEIVE)
		);

		packetHandler.receive(null, clientNetworkHandler, payload, packetSender);

		assertIterableEquals(List.of(SERVER_RECEIVE), channelInfoHolder.getPendingChannelsNames(NetworkState.PLAY));

		// Check the response we are sending back to the server
		PacketByteBuf response = readResponse(packetSender);
		assertEquals(1, response.readVarInt());
		assertEquals("play", response.readString());
		assertIterableEquals(List.of(CLIENT_RECEIVE), response.readCollection(HashSet::new, PacketByteBuf::readIdentifier));
		assertEquals(0, response.readableBytes());
	}

	// Test handling the configuration registry packet on the client configuration handler
	@Test
	void handleConfigurationRegistryClient() {
		var packetHandler = ((ClientConfigurationNetworking.CustomChannelReceiver<CommonRegisterPayload>) ClientNetworkingImpl.CONFIGURATION.getReceiver(CommonRegisterPayload.PACKET_ID));
		assertNotNull(packetHandler);

		when(clientAddon.getNegotiatedVersion()).thenReturn(1);
		when(clientAddon.createRegisterPayload()).thenAnswer(i -> new CommonRegisterPayload(1, "configuration", Set.of(CLIENT_RECEIVE_CONFIGURATION)));

		// Receive a packet from the server
		CommonRegisterPayload payload = new CommonRegisterPayload(
			1,
			"configuration",
			Set.of(SERVER_RECEIVE)
		);

		packetHandler.receive(null, clientNetworkHandler, payload, packetSender);

		verify(clientAddon, times(1)).onCommonRegisterPacket(any());

		// Check the response we are sending back to the server
		PacketByteBuf response = readResponse(packetSender);
		assertEquals(1, response.readVarInt());
		assertEquals("configuration", response.readString());
		assertIterableEquals(List.of(CLIENT_RECEIVE_CONFIGURATION), response.readCollection(HashSet::new, PacketByteBuf::readIdentifier));
		assertEquals(0, response.readableBytes());
	}

	// Test handing the play registry packet on the server configuration handler
	@Test
	void handlePlayRegistryServer() {
		var packetHandler = ((ServerConfigurationNetworking.CustomChannelReceiver<CommonRegisterPayload>) ServerNetworkingImpl.CONFIGURATION.getReceiver(CommonRegisterPayload.PACKET_ID));
		assertNotNull(packetHandler);

		when(serverAddon.getNegotiatedVersion()).thenReturn(1);

		// Receive a packet from the client
		CommonRegisterPayload payload = new CommonRegisterPayload(
			1,
			"play",
			Set.of(SERVER_RECEIVE)
		);

		packetHandler.receive(null, serverNetworkHandler, payload, packetSender);

		// Assert the entire packet was read
		assertIterableEquals(List.of(SERVER_RECEIVE), channelInfoHolder.getPendingChannelsNames(NetworkState.PLAY));
	}

	// Test handing the configuration registry packet on the server configuration handler
	@Test
	void handleConfigurationRegistryServer() {
		var packetHandler = ((ServerConfigurationNetworking.CustomChannelReceiver<CommonRegisterPayload>) ServerNetworkingImpl.CONFIGURATION.getReceiver(CommonRegisterPayload.PACKET_ID));
		assertNotNull(packetHandler);

		when(serverAddon.getNegotiatedVersion()).thenReturn(1);

		// Receive a packet from the client
		CommonRegisterPayload payload = new CommonRegisterPayload(
			1,
			"configuration",
			Set.of(SERVER_RECEIVE)
		);

		packetHandler.receive(null, serverNetworkHandler, payload, packetSender);

		// Assert the entire packet was read
		verify(serverAddon, times(1)).onCommonRegisterPacket(any());
	}

	@Test
	public void testHighestCommonVersionWithCommonElement() {
		int[] a = {1, 2, 3};
		int[] b = {1, 2};
		assertEquals(2, CommonPacketsImpl.getHighestCommonVersion(a, b));
	}

	@Test
	public void testHighestCommonVersionWithoutCommonElement() {
		int[] a = {1, 3, 5};
		int[] b = {2, 4, 6};
		assertEquals(-1, CommonPacketsImpl.getHighestCommonVersion(a, b));
	}

	@Test
	public void testHighestCommonVersionWithOneEmptyArray() {
		int[] a = {1, 3, 5};
		int[] b = {};
		assertEquals(-1, CommonPacketsImpl.getHighestCommonVersion(a, b));
	}

	@Test
	public void testHighestCommonVersionWithBothEmptyArrays() {
		int[] a = {};
		int[] b = {};
		assertEquals(-1, CommonPacketsImpl.getHighestCommonVersion(a, b));
	}

	@Test
	public void testHighestCommonVersionWithIdenticalArrays() {
		int[] a = {1, 2, 3};
		int[] b = {1, 2, 3};
		assertEquals(3, CommonPacketsImpl.getHighestCommonVersion(a, b));
	}

	private static PacketByteBuf readResponse(PacketSender<CustomPayload> packetSender) {
		ArgumentCaptor<CustomPayload> responseCaptor = ArgumentCaptor.forClass(CustomPayload.class);
		verify(packetSender, times(1)).sendPayload(responseCaptor.capture());

		PacketByteBuf buf = PacketByteBufs.create();
		responseCaptor.getValue().write(buf);

		return buf;
	}

	private static int getNegotiatedVersion(CommonPacketHandler packetHandler) {
		ArgumentCaptor<Integer> responseCaptor = ArgumentCaptor.forClass(Integer.class);
		verify(packetHandler, times(1)).onCommonVersionPacket(responseCaptor.capture());
		return responseCaptor.getValue();
	}

	private static class MockChannelInfoHolder implements ChannelInfoHolder {
		private final Map<NetworkState, Collection<Identifier>> playChannels = new ConcurrentHashMap<>();

		@Override
		public Collection<Identifier> getPendingChannelsNames(NetworkState state) {
			return this.playChannels.computeIfAbsent(state, (key) -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
		}
	}
}
