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
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.PayloadTypeRegistry;
import org.quiltmc.qsl.networking.api.client.ClientConfigurationNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.networking.api.server.ServerConfigurationNetworking;
import org.quiltmc.qsl.networking.impl.ChannelInfoHolder;
import org.quiltmc.qsl.networking.impl.client.ClientConfigurationNetworkAddon;
import org.quiltmc.qsl.networking.impl.client.ClientNetworkingImpl;
import org.quiltmc.qsl.networking.impl.common.CommonPacketHandler;
import org.quiltmc.qsl.networking.impl.common.CommonPacketsImpl;
import org.quiltmc.qsl.networking.impl.common.CommonRegisterPayload;
import org.quiltmc.qsl.networking.impl.common.CommonVersionPayload;
import org.quiltmc.qsl.networking.impl.server.ServerConfigurationNetworkAddon;
import org.quiltmc.qsl.networking.impl.server.ServerNetworkingImpl;

// Last updated from commit ab7edba
@SuppressWarnings("unchecked")
public class CommonPacketTests {
	private static final CustomPayload.Type<PacketByteBuf, CommonVersionPayload> VERSION_PAYLOAD_TYPE = new CustomPayload.Type<>(CommonVersionPayload.PACKET_ID, CommonVersionPayload.CODEC);
	private static final CustomPayload.Type<PacketByteBuf, CommonRegisterPayload> REGISTER_PAYLOAD_TYPE = new CustomPayload.Type<>(CommonRegisterPayload.PACKET_ID, CommonRegisterPayload.CODEC);

	private PacketSender packetSender;
	private ChannelInfoHolder channelInfoHolder;

	private ClientConfigurationNetworkHandler clientNetworkHandler;
	private ClientConfigurationNetworkAddon clientAddon;

	private ServerConfigurationNetworkHandler serverNetworkHandler;
	private ServerConfigurationNetworkAddon serverAddon;

	private static final CustomPayload.Id<TestPayload> CLIENT_RECEIVE = new CustomPayload.Id<>(Identifier.of("quilt", "global_client"));
	private static final CustomPayload.Id<?> CLIENT_RECEIVE_CONFIGURATION = new CustomPayload.Id<>(Identifier.of("quilt", "global_configuration_client"));
	private static final Identifier SERVER_RECEIVE_ID = Identifier.of("quilt", "test");
	private static final CustomPayload.Id<?> SERVER_RECEIVE = new CustomPayload.Id<>(SERVER_RECEIVE_ID);

	@BeforeAll
	static void beforeAll() {
		CommonPacketsImpl.init(null);
		ClientNetworkingImpl.clientInit(null);

		// Register the packet codec on both sides
		PayloadTypeRegistry.playS2C().register(TestPayload.ID, TestPayload.CODEC);

		// Listen for the payload on the client
		ClientPlayNetworking.registerGlobalReceiver(TestPayload.ID, (client, handler, payload, responseSender) -> {
			System.out.println(payload.data());
		});
	}

	private record TestPayload(String data) implements CustomPayload {
		static final CustomPayload.Id<TestPayload> ID = CLIENT_RECEIVE;
		static final PacketCodec<RegistryByteBuf, TestPayload> CODEC = CustomPayload.create(TestPayload::write, TestPayload::new);

		TestPayload(RegistryByteBuf buf) {
			this(buf.readString());
		}

		private void write(RegistryByteBuf buf) {
			buf.writeString(data);
		}

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	@BeforeEach
	void setUp() {
		packetSender = mock(PacketSender.class);
		channelInfoHolder = new MockChannelInfoHolder();

		clientNetworkHandler = mock(ClientConfigurationNetworkHandler.class);
		clientAddon = mock(ClientConfigurationNetworkAddon.class);
		when(ClientNetworkingImpl.getAddon(clientNetworkHandler)).thenReturn(clientAddon);
		when(clientAddon.getChannelInfoHolder()).thenReturn(channelInfoHolder);

		serverNetworkHandler = mock(ServerConfigurationNetworkHandler.class);
		serverAddon = mock(ServerConfigurationNetworkAddon.class);
		when(ServerNetworkingImpl.getAddon(serverNetworkHandler)).thenReturn(serverAddon);
		when(serverAddon.getChannelInfoHolder()).thenReturn(channelInfoHolder);

		ClientNetworkingImpl.setClientConfigurationAddon(clientAddon);
	}

	// Test handling the version packet on the client
	@Test
	void handleVersionPacketClient() {
		var packetHandler = (ClientConfigurationNetworking.CustomChannelReceiver<CommonVersionPayload>) ClientNetworkingImpl.CONFIGURATION.getReceiver(CommonVersionPayload.PACKET_ID);
		assertNotNull(packetHandler);

		// Receive a packet from the server
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeIntArray(new int[]{1, 2, 3});

		CommonVersionPayload payload = CommonVersionPayload.CODEC.decode(buf);
		packetHandler.receive(null, clientNetworkHandler, payload, packetSender);

		// Assert the entire packet was read
		assertEquals(0, buf.readableBytes());

		// Check the response we are sending back to the server
		PacketByteBuf response = readResponse(packetSender, VERSION_PAYLOAD_TYPE);
		assertArrayEquals(new int[]{1}, response.readIntArray());
		assertEquals(0, response.readableBytes());

		assertEquals(1, getNegotiatedVersion(clientAddon));
	}

	// Test handling the version packet on the client, when the server sends unsupported versions
	@Test
	void handleVersionPacketClientUnsupported() {
		var packetHandler = (ClientConfigurationNetworking.CustomChannelReceiver<CommonVersionPayload>) ClientNetworkingImpl.CONFIGURATION.getReceiver(CommonVersionPayload.PACKET_ID);
		assertNotNull(packetHandler);

		// Receive a packet from the server
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeIntArray(new int[]{2, 3}); // We only support version 1

		assertThrows(UnsupportedOperationException.class, () -> {
			CommonVersionPayload payload = CommonVersionPayload.CODEC.decode(buf);
			packetHandler.receive(null, clientNetworkHandler, payload, packetSender);
		});

		// Assert the entire packet was read
		assertEquals(0, buf.readableBytes());
	}

	// Test handling the version packet on the server
	@Test
	void handleVersionPacketServer() {
		var packetHandler = (ServerConfigurationNetworking.CustomChannelReceiver<CommonVersionPayload>) ServerNetworkingImpl.CONFIGURATION.getReceiver(CommonVersionPayload.PACKET_ID);
		assertNotNull(packetHandler);

		// Receive a packet from the client
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeIntArray(new int[]{1, 2, 3});

		CommonVersionPayload payload = CommonVersionPayload.CODEC.decode(buf);
		packetHandler.receive(null, serverNetworkHandler, payload, packetSender);

		// Assert the entire packet was read
		assertEquals(0, buf.readableBytes());
		assertEquals(1, getNegotiatedVersion(serverAddon));
	}

	// Test handling the version packet on the server unsupported version
	@Test
	void handleVersionPacketServerUnsupported() {
		var packetHandler = (ServerConfigurationNetworking.CustomChannelReceiver<CommonVersionPayload>) ServerNetworkingImpl.CONFIGURATION.getReceiver(CommonVersionPayload.PACKET_ID);
		assertNotNull(packetHandler);

		// Receive a packet from the client
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeIntArray(new int[]{3}); // Server only supports version 1

		assertThrows(UnsupportedOperationException.class, () -> {
			CommonVersionPayload payload = CommonVersionPayload.CODEC.decode(buf);
			packetHandler.receive(null, serverNetworkHandler, payload, packetSender);
		});

		// Assert the entire packet was read
		assertEquals(0, buf.readableBytes());
	}

	// Test handing the play registry packet on the client configuration handler
	@Test
	void handlePlayRegistryClient() {
		var packetHandler = (ClientConfigurationNetworking.CustomChannelReceiver<CommonRegisterPayload>) ClientNetworkingImpl.CONFIGURATION.getReceiver(CommonRegisterPayload.PACKET_ID);
		assertNotNull(packetHandler);

		when(clientAddon.getNegotiatedVersion()).thenReturn(1);

		// Receive a packet from the server
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(1); // Version
		buf.writeString("play"); // Target phase
		buf.writeCollection(List.of(SERVER_RECEIVE_ID), PacketByteBuf::writeIdentifier);

		CommonRegisterPayload payload = CommonRegisterPayload.CODEC.decode(buf);
		packetHandler.receive(null, clientNetworkHandler, payload, packetSender);

		// Assert the entire packet was read
		assertEquals(0, buf.readableBytes());
		assertIterableEquals(List.of(SERVER_RECEIVE), channelInfoHolder.getPendingChannelsNames(NetworkPhase.PLAY));

		// Check the response we are sending back to the server
		PacketByteBuf response = readResponse(packetSender, REGISTER_PAYLOAD_TYPE);
		assertEquals(1, response.readVarInt());
		assertEquals("play", response.readString());
		assertIterableEquals(List.of(CLIENT_RECEIVE), response.readCollection(HashSet::new, _buf -> new CustomPayload.Id<>(_buf.readIdentifier())));
		assertEquals(0, response.readableBytes());
	}

	// Test handling the configuration registry packet on the client configuration handler
	@Test
	void handleConfigurationRegistryClient() {
		var packetHandler = (ClientConfigurationNetworking.CustomChannelReceiver<CommonRegisterPayload>) ClientNetworkingImpl.CONFIGURATION.getReceiver(CommonRegisterPayload.PACKET_ID);
		assertNotNull(packetHandler);

		when(clientAddon.getNegotiatedVersion()).thenReturn(1);
		when(clientAddon.createRegisterPayload()).thenAnswer(i -> new CommonRegisterPayload(1, "configuration", Set.of(CLIENT_RECEIVE_CONFIGURATION)));

		// Receive a packet from the server
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(1); // Version
		buf.writeString("configuration"); // Target phase
		buf.writeCollection(List.of(SERVER_RECEIVE_ID), PacketByteBuf::writeIdentifier);

		CommonRegisterPayload payload = CommonRegisterPayload.CODEC.decode(buf);
		packetHandler.receive(null, clientNetworkHandler, payload, packetSender);

		// Assert the entire packet was read
		assertEquals(0, buf.readableBytes());
		verify(clientAddon, times(1)).onCommonRegisterPacket(any());

		// Check the response we are sending back to the server
		PacketByteBuf response = readResponse(packetSender, REGISTER_PAYLOAD_TYPE);
		assertEquals(1, response.readVarInt());
		assertEquals("configuration", response.readString());
		assertIterableEquals(List.of(CLIENT_RECEIVE_CONFIGURATION), response.readCollection(HashSet::new, _buf -> new CustomPayload.Id<>(_buf.readIdentifier())));
		assertEquals(0, response.readableBytes());
	}

	// Test handing the play registry packet on the server configuration handler
	@Test
	void handlePlayRegistryServer() {
		var packetHandler = (ServerConfigurationNetworking.CustomChannelReceiver<CommonRegisterPayload>) ServerNetworkingImpl.CONFIGURATION.getReceiver(CommonRegisterPayload.PACKET_ID);
		assertNotNull(packetHandler);

		when(serverAddon.getNegotiatedVersion()).thenReturn(1);

		// Receive a packet from the client
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(1); // Version
		buf.writeString("play"); // Target phase
		buf.writeCollection(List.of(SERVER_RECEIVE_ID), PacketByteBuf::writeIdentifier);

		CommonRegisterPayload payload = CommonRegisterPayload.CODEC.decode(buf);
		packetHandler.receive(null, serverNetworkHandler, payload, packetSender);

		// Assert the entire packet was read
		assertEquals(0, buf.readableBytes());
		assertIterableEquals(List.of(SERVER_RECEIVE), channelInfoHolder.getPendingChannelsNames(NetworkPhase.PLAY));
	}

	// Test handing the configuration registry packet on the server configuration handler
	@Test
	void handleConfigurationRegistryServer() {
		var packetHandler = (ServerConfigurationNetworking.CustomChannelReceiver<CommonRegisterPayload>) ServerNetworkingImpl.CONFIGURATION.getReceiver(CommonRegisterPayload.PACKET_ID);
		assertNotNull(packetHandler);

		when(serverAddon.getNegotiatedVersion()).thenReturn(1);

		// Receive a packet from the client
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeVarInt(1); // Version
		buf.writeString("configuration"); // Target phase
		buf.writeCollection(List.of(SERVER_RECEIVE_ID), PacketByteBuf::writeIdentifier);

		CommonRegisterPayload payload = CommonRegisterPayload.CODEC.decode(buf);
		packetHandler.receive(null, serverNetworkHandler, payload, packetSender);

		// Assert the entire packet was read
		assertEquals(0, buf.readableBytes());
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

	private static <T extends CustomPayload> PacketByteBuf readResponse(PacketSender packetSender, CustomPayload.Type<PacketByteBuf, T> type) {
		ArgumentCaptor<CustomPayload> responseCaptor = ArgumentCaptor.forClass(CustomPayload.class);
		verify(packetSender, times(1)).sendPayload(responseCaptor.capture());

		final T payload = (T) responseCaptor.getValue();
		final PacketByteBuf buf = PacketByteBufs.create();
		type.codec().encode(buf, payload);

		return buf;
	}

	private static int getNegotiatedVersion(CommonPacketHandler packetHandler) {
		ArgumentCaptor<Integer> responseCaptor = ArgumentCaptor.forClass(Integer.class);
		verify(packetHandler, times(1)).onCommonVersionPacket(responseCaptor.capture());
		return responseCaptor.getValue();
	}

	private static class MockChannelInfoHolder implements ChannelInfoHolder {
		private final Map<NetworkPhase, Collection<CustomPayload.Id<?>>> playChannels = new ConcurrentHashMap<>();

		@Override
		public Collection<CustomPayload.Id<?>> getPendingChannelsNames(NetworkPhase state) {
			return this.playChannels.computeIfAbsent(state, (key) -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
		}
	}
}
