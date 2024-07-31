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

package org.quiltmc.qsl.networking.test.play;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.List;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.network.packet.s2c.PacketBundleS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PayloadTypeRegistry;
import org.quiltmc.qsl.networking.api.server.ServerPlayNetworking;
import org.quiltmc.qsl.networking.test.NetworkingTestMods;

public final class NetworkingPlayPacketTest implements ModInitializer {
	public static final CustomPayload.Id<TestPacket> TEST_CHANNEL = NetworkingTestMods.id("test_channel");
	public static final PacketCodec<PacketByteBuf, TestPacket> TEST_CODEC = CustomPayload.create(TestPacket::write, TestPacket::new);

	public static void sendToTestChannel(ServerPlayerEntity player, String stuff) {
		ServerPlayNetworking.send(player, new TestPacket(stuff));
		NetworkingTestMods.LOGGER.info("Sent custom payload packet in {}", TEST_CHANNEL);
	}

	public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
		NetworkingTestMods.LOGGER.info("Registering test command");

		dispatcher.register(literal("network_test_command")
				.then(argument("stuff", string()).executes(ctx -> {
					String stuff = StringArgumentType.getString(ctx, "stuff");
					sendToTestChannel(ctx.getSource().getPlayer(), stuff);
					return Command.SINGLE_SUCCESS;
				}))
				.then(literal("bundled").executes(ctx -> {
					PacketByteBuf bufA = PacketByteBufs.create();
					bufA.writeString("Bundled #1");
					PacketByteBuf bufB = PacketByteBufs.create();
					bufB.writeString("Bundled #2");

					var packet = new PacketBundleS2CPacket(List.of(
							(Packet<ClientPlayPacketListener>) (Object) ServerPlayNetworking.createS2CPacket(new TestPacket("Bundled #1")),
							(Packet<ClientPlayPacketListener>) (Object) ServerPlayNetworking.createS2CPacket(new TestPacket("Bundled #2"))
					));
					ctx.getSource().getPlayer().networkHandler.send(packet);
					return Command.SINGLE_SUCCESS;
				})));
	}

	@Override
	public void onInitialize(ModContainer mod) {
		NetworkingTestMods.LOGGER.info("Hello from networking user!");
		PayloadTypeRegistry.playS2C().register(TEST_CHANNEL, TEST_CODEC);

		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) -> {
			NetworkingPlayPacketTest.registerCommand(dispatcher);
		});
	}

	public record TestPacket(String text) implements CustomPayload {
		public TestPacket(PacketByteBuf buf) {
			this(buf.readString());
		}

		private void write(PacketByteBuf buf) {
			buf.writeString(this.text);
		}

		@Override
		public Id<? extends CustomPayload> getId() {
			return TEST_CHANNEL;
		}
	}
}
