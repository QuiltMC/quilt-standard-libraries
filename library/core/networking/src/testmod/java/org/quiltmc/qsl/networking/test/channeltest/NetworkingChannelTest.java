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

package org.quiltmc.qsl.networking.test.channeltest;

import static net.minecraft.command.argument.EntityArgumentType.getPlayer;
import static net.minecraft.command.argument.EntityArgumentType.player;
import static net.minecraft.command.argument.IdentifierArgumentType.getIdentifier;
import static net.minecraft.command.argument.IdentifierArgumentType.identifier;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.command.CommandBuildContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelector;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

public final class NetworkingChannelTest implements CommandRegistrationCallback {
	@Override
	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandBuildContext buildContext, CommandManager.RegistrationEnvironment environment) {
		final LiteralCommandNode<ServerCommandSource> channelTestCommand = literal("network_channel_test").build();

		// Info
		{
			final LiteralCommandNode<ServerCommandSource> info = literal("info")
					.executes(context -> infoCommand(context, context.getSource().getPlayer()))
					.build();

			final ArgumentCommandNode<ServerCommandSource, EntitySelector> player = argument("player", player())
					.executes(context -> infoCommand(context, getPlayer(context, "player")))
					.build();

			info.addChild(player);
			channelTestCommand.addChild(info);
		}

		// Register
		{
			final LiteralCommandNode<ServerCommandSource> register = literal("register")
					.then(argument("channel", identifier())
							.executes(context -> registerChannel(context, context.getSource().getPlayer())))
					.build();

			channelTestCommand.addChild(register);
		}

		// Unregister
		{
			final LiteralCommandNode<ServerCommandSource> unregister = literal("unregister")
					.then(argument("channel", identifier()).suggests(NetworkingChannelTest::suggestReceivableChannels)
							.executes(context -> unregisterChannel(context, context.getSource().getPlayer())))
					.build();

			channelTestCommand.addChild(unregister);
		}

		dispatcher.getRoot().addChild(channelTestCommand);
	}

	private static CompletableFuture<Suggestions> suggestReceivableChannels(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
		final ServerPlayerEntity player = context.getSource().getPlayer();

		return CommandSource.suggestIdentifiers(ServerPlayNetworking.getReceived(player), builder);
	}

	private static int registerChannel(CommandContext<ServerCommandSource> context, ServerPlayerEntity executor) throws CommandSyntaxException {
		final Identifier channel = getIdentifier(context, "channel");

		if (ServerPlayNetworking.getReceived(executor).contains(channel)) {
			throw new SimpleCommandExceptionType(Text.of(String.format("Cannot register channel %s twice for server player", channel))).create();
		}

		ServerPlayNetworking.registerReceiver(executor.networkHandler, channel, (server, player, handler, buf, sender) -> {
			System.out.printf("Received packet on channel %s%n", channel);
		});

		context.getSource().sendSystemMessage(Text.of(String.format("Registered channel %s for %s", channel, executor.getEntityName())));

		return 1;
	}

	private static int unregisterChannel(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) throws CommandSyntaxException {
		final Identifier channel = getIdentifier(context, "channel");

		if (!ServerPlayNetworking.getReceived(player).contains(channel)) {
			throw new SimpleCommandExceptionType(Text.of("Cannot unregister channel the server player entity cannot recieve packets on")).create();
		}

		ServerPlayNetworking.unregisterReceiver(player.networkHandler, channel);
		context.getSource().sendSystemMessage(Text.of(String.format("Unregistered channel %s for %s", getIdentifier(context, "channel"), player.getEntityName())));

		return 1;
	}

	private static int infoCommand(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
		ServerCommandSource source = context.getSource();
		Set<Identifier> channels = ServerPlayNetworking.getSendable(player);

		source.sendSystemMessage(Text.of(String.format("Available channels for player %s", player.getEntityName())));

		for (Identifier channel : channels) {
			source.sendSystemMessage(Text.of(channel.toString()));
		}

		return 1;
	}
}
