/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.command.impl.client;

import com.google.common.collect.Iterables;
import com.mojang.brigadier.AmbiguityConsumer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.BuiltInExceptionProvider;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.command.api.client.ClientCommandManager;
import org.quiltmc.qsl.command.api.client.ClientCommandRegistrationCallback;
import org.quiltmc.qsl.command.api.client.QuiltClientCommandSource;
import org.quiltmc.qsl.command.mixin.HelpCommandAccessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.quiltmc.qsl.command.api.client.ClientCommandManager.*;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
public final class ClientCommandInternals {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final char PREFIX = '/';
	private static final String API_COMMAND_NAME = "quilt_commands:client_commands";
	private static final String SHORT_API_COMMAND_NAME = "qcc";
	private static final Command<QuiltClientCommandSource> DUMMY_COMMAND = ctx -> 0;

	/**
	 * Executes a client-sided command from a message.
	 *
	 * @param 	message	the command message
	 *
	 * @return 	true if the message was executed as a command client-side (and therefore should not be sent to the
	 * 			server), false otherwise
	 */
	public static boolean executeCommand(String message) {
		if (message.isEmpty()) {
			return false; // Nothing to process
		}

		if (message.charAt(0) != PREFIX) {
			return false; // Incorrect prefix, won't execute anything.
		}

		MinecraftClient client = MinecraftClient.getInstance();

		// The interface is implemented on ClientCommandSource with a mixin.
		// noinspection ConstantConditions
		QuiltClientCommandSource commandSource = (QuiltClientCommandSource) client.getNetworkHandler().getCommandSource();

		client.getProfiler().push(message);

		try {
			// Only run client commands if there are no matching server-side commands.
			String command = message.substring(1);
			CommandDispatcher<CommandSource> serverDispatcher = client.getNetworkHandler().getCommandDispatcher();
			ParseResults<CommandSource> serverResults = serverDispatcher.parse(command, client.getNetworkHandler().getCommandSource());

			if (serverResults.getReader().canRead() || isCommandInvalidOrDummy(serverResults)) {
				DISPATCHER.execute(command, commandSource);
				return true;
			} else {
				return false;
			}
		} catch (CommandSyntaxException e) {
			boolean ignored = shouldIgnore(e.getType());
			LOGGER.log(ignored ? Level.DEBUG : Level.WARN, "Syntax exception for client-side command '{}'", message, e);

			if (ignored) {
				return false;
			}

			commandSource.sendError(getErrorMessage(e));
			return true;
		} catch (CommandException e) {
			LOGGER.warn("Error while executing client-side command '{}'", message, e);
			commandSource.sendError(e.getTextMessage());
			return true;
		} catch (RuntimeException e) {
			LOGGER.warn("Error while executing client-side command '{}'", message, e);
			commandSource.sendError(Text.of(e.getMessage()));
			return true;
		} finally {
			client.getProfiler().pop();
		}
	}

	/**
	 * Tests whether a parse result is invalid or the command it resolves to is a dummy command.
	 *
	 * Used to work out whether a command in the main dispatcher is a dummy command added by {@link ClientCommandInternals#addDummyCommands(CommandDispatcher, QuiltClientCommandSource)}.
	 *
	 * @param parse the parse results to test.
	 * @param <S> 	the command source type.
	 *
	 * @return true if the parse result is invalid or the command is a dummy, false otherwise.
	 */
	public static <S extends CommandSource> boolean isCommandInvalidOrDummy(final ParseResults<S> parse) {
		if (parse.getReader().canRead()) {
			return true;
		}

		final String command = parse.getReader().getString();
		final CommandContext<S> context = parse.getContext().build(command);

		return context.getCommand() == null || context.getCommand() == DUMMY_COMMAND;
	}

	/**
	 * Tests whether a command syntax exception with the type
	 * should be ignored and the message sent to the server.
	 *
	 * @param type the exception type
	 * @return true if ignored, false otherwise
	 */
	private static boolean shouldIgnore(CommandExceptionType type) {
		BuiltInExceptionProvider builtins = CommandSyntaxException.BUILT_IN_EXCEPTIONS;

		// Only ignore unknown commands and node parse exceptions.
		// The argument-related dispatcher exceptions are not ignored because
		// they will only happen if the user enters a correct command.
		return type == builtins.dispatcherUnknownCommand() || type == builtins.dispatcherParseException();
	}

	/**
	 * Analogous to {@code CommandSuggestor#formatException}, but returns a {@link Text} rather than an
	 * {@link net.minecraft.text.OrderedText OrderedText}.
	 *
	 * @param e the exception to get the error message from
	 *
	 * @return the error message as a {@link Text}
	 */
	private static Text getErrorMessage(CommandSyntaxException e) {
		Text message = Texts.toText(e.getRawMessage());
		String context = e.getContext();

		return context != null ? new TranslatableText("command.context.parse_error", message, context) : message;
	}

	/**
	 * Registers client-sided commands, then runs final initialization tasks such as
	 * {@link CommandDispatcher#findAmbiguities(AmbiguityConsumer)} on the command dispatcher. Also registers
	 * a {@code /qcc help} command if there are other commands present.
	 */
	public static void initialize() {
		ClientCommandRegistrationCallback.EVENT.invoker().registerCommands(DISPATCHER);

		if (!DISPATCHER.getRoot().getChildren().isEmpty()) {
			// Register the qcc command only if there are other commands;
			// it is not needed if there are no client commands.
			CommandNode<QuiltClientCommandSource> mainNode = DISPATCHER.register(
					literal(API_COMMAND_NAME)
							.then(createHelpCommand())
							.then(createRunCommand())
			);
			DISPATCHER.register(literal(SHORT_API_COMMAND_NAME).redirect(mainNode));
		}

		DISPATCHER.findAmbiguities((parent, child, sibling, inputs) ->
				LOGGER.warn("Ambiguity between arguments {} and {} with inputs: {}", DISPATCHER.getPath(child), DISPATCHER.getPath(sibling), inputs)
		);
	}

	/**
	 * @return the {@code run} subcommand for {@code /qcc}
	 */
	private static LiteralArgumentBuilder<QuiltClientCommandSource> createRunCommand() {
		LiteralArgumentBuilder<QuiltClientCommandSource> runCommand = literal("run");
		for (CommandNode<QuiltClientCommandSource> node : ClientCommandManager.DISPATCHER.getRoot().getChildren()) {
			runCommand.then(node);
		}
		return runCommand;
	}

	/**
	 * @return the {@code help} subcommand for {@code /qcc}
	 */
	private static LiteralArgumentBuilder<QuiltClientCommandSource> createHelpCommand() {
		return literal("help").then(
						argument("command", StringArgumentType.greedyString())
								.executes(ClientCommandInternals::executeSpecificHelp))
				.executes(ClientCommandInternals::executeRootHelp);
	}

	/**
	 * Runs {@link #executeHelp(CommandNode, CommandContext)} on the root node of the client-side command dispatcher.
	 *
	 * @param context the command context
	 *
	 * @return the number of commands
	 */
	private static int executeRootHelp(CommandContext<QuiltClientCommandSource> context) {
		return executeHelp(DISPATCHER.getRoot(), context);
	}

	/**
	 * Runs {@link #executeHelp(CommandNode, CommandContext)} on a specific client-side command specified as a command
	 * argument.
	 *
	 * @param context the command context
	 *
	 * @return the number of subcommands of the command specified in the command argument
	 *
	 * @throws CommandSyntaxException if no such command as given in the command argument is given
	 */
	private static int executeSpecificHelp(CommandContext<QuiltClientCommandSource> context) throws CommandSyntaxException {
		ParseResults<QuiltClientCommandSource> parseResults = DISPATCHER.parse(StringArgumentType.getString(context, "command"), context.getSource());
		List<ParsedCommandNode<QuiltClientCommandSource>> nodes = parseResults.getContext().getNodes();

		if (nodes.isEmpty()) {
			throw HelpCommandAccessor.getFailedException().create();
		}

		// we know the call to Iterables#getLast won't be null as nodes is guaranteed to be nonempty
		return executeHelp(Objects.requireNonNull(Iterables.getLast(nodes)).getNode(), context);
	}

	/**
	 * Shows usage hints for a command node.
	 *
	 * @param startNode the node to get the usages of
	 * @param context the command context
	 *
	 * @return the amount of usage hints (i.e. the number of subcommands of startNode)
	 */
	private static int executeHelp(CommandNode<QuiltClientCommandSource> startNode, CommandContext<QuiltClientCommandSource> context) {
		Map<CommandNode<QuiltClientCommandSource>, String> commands = DISPATCHER.getSmartUsage(startNode, context.getSource());

		for (String command : commands.values()) {
			context.getSource().sendFeedback(new LiteralText(PREFIX + command));
		}

		return commands.size();
	}

	/**
	 * Adds dummy versions of the client commands to a given command dispatcher. Used to add the commands to
	 * {@link ClientPlayNetworkHandler}'s command dispatcher for autocompletion.
	 *
	 * @param target the target command dispatcher
	 * @param source the command source - commands which the source cannot use are filtered out
	 */
	public static void addDummyCommands(CommandDispatcher<QuiltClientCommandSource> target, QuiltClientCommandSource source) {
		Map<CommandNode<QuiltClientCommandSource>, CommandNode<QuiltClientCommandSource>> originalToCopy = new HashMap<>();
		originalToCopy.put(DISPATCHER.getRoot(), target.getRoot());
		copyChildren(DISPATCHER.getRoot(), target.getRoot(), source, originalToCopy);
	}

	/**
	 * Copies the child commands from origin to target, filtered by {@code child.canUse(source)}.
	 * Mimics vanilla's {@code CommandManager#makeTreeForSource}. Runs recursively.
	 *
	 * @param origin         the source command node
	 * @param target         the target command node
	 * @param source         the command source
	 * @param originalToCopy a mutable map from original command nodes to their copies, used for redirects;
	 *                       should contain a mapping from origin to target
	 */
	private static void copyChildren(
			CommandNode<QuiltClientCommandSource> origin,
			CommandNode<QuiltClientCommandSource> target,
			QuiltClientCommandSource source,
			Map<CommandNode<QuiltClientCommandSource>, CommandNode<QuiltClientCommandSource>> originalToCopy
	) {
		for (CommandNode<QuiltClientCommandSource> child : origin.getChildren()) {
			if (!child.canUse(source)) continue;

			if (target.getChild(child.getName()) != null) {
				continue;
			}

			ArgumentBuilder<QuiltClientCommandSource, ?> builder = child.createBuilder();

			// Reset the unnecessary non-completion stuff from the builder
			builder.requires(s -> true); // This is checked with the if check above.

			if (builder.getCommand() != null) {
				builder.executes(DUMMY_COMMAND);
			}

			// Set up redirects
			if (builder.getRedirect() != null) {
				builder.redirect(originalToCopy.get(builder.getRedirect()));
			}

			CommandNode<QuiltClientCommandSource> result = builder.build();
			originalToCopy.put(child, result);
			target.addChild(result);

			if (!child.getChildren().isEmpty()) {
				copyChildren(child, result, source, originalToCopy);
			}
		}
	}
}
