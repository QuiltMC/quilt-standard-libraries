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
import net.minecraft.network.packet.s2c.PacketBundleS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.test.NetworkingTestMods;

public final class NetworkingPlayPacketTest implements ModInitializer {
	public static final Identifier TEST_CHANNEL = NetworkingTestMods.id("test_channel");

	public static void sendToTestChannel(ServerPlayerEntity player, String stuff) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeText(Text.of(stuff));
		ServerPlayNetworking.send(player, TEST_CHANNEL, buf);
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
					bufA.writeText(Text.literal("Bundled #1"));
					PacketByteBuf bufB = PacketByteBufs.create();
					bufB.writeText(Text.literal("Bundled #2"));

					var packet = new PacketBundleS2CPacket(List.of(
							ServerPlayNetworking.createS2CPacket(TEST_CHANNEL, bufA),
							ServerPlayNetworking.createS2CPacket(TEST_CHANNEL, bufB)
					));
					ctx.getSource().getPlayer().networkHandler.sendPacket(packet);
					return Command.SINGLE_SUCCESS;
				})));
	}

	@Override
	public void onInitialize(ModContainer mod) {
		NetworkingTestMods.LOGGER.info("Hello from networking user!");

		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) -> {
			NetworkingPlayPacketTest.registerCommand(dispatcher);
		});
	}
}
