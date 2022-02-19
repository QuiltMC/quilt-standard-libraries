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

package org.quiltmc.qsl.networking.test.keybindreciever;

import net.fabricmc.loader.api.ModContainer;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.KeyBindText;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.test.NetworkingTestMods;

// Listens for a packet from the client which is sent to the server when a keybinding is pressed.
// In response the server will send a message containing the keybind text letting the client know it pressed that key.
public final class NetworkingKeyBindPacketTest implements ModInitializer {
	public static final Identifier KEYBINDING_PACKET_ID = NetworkingTestMods.id("keybind_press_test");

	private static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		// TODO: Can we send chat off the server thread?
		server.execute(() -> {
			player.sendMessage(new LiteralText("You pressed ").append(new KeyBindText("quilt_networking_keybind_testmod").styled(style -> style.withFormatting(Formatting.BLUE))), false);
		});
	}

	@Override
	public void onInitialize(ModContainer mod) {
		ServerPlayConnectionEvents.INIT.register((handler, server) -> {
			ServerPlayNetworking.registerReceiver(handler, KEYBINDING_PACKET_ID, NetworkingKeyBindPacketTest::receive);
		});
	}
}
