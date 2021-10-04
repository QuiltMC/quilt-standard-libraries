/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.registry.attribute.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.registry.attribute.api.RegistryEntryAttribute;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

@ApiStatus.Internal
public final class DumpBuiltinAttributesCommand {
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static final class RegistryArgumentType implements ArgumentType<Registry<?>> {
		private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar");
		public static final DynamicCommandExceptionType UNKNOWN_REGISTRY_EXCEPTION = new DynamicCommandExceptionType(
				o -> new LiteralText("Could not find registry " + o));

		public static RegistryArgumentType registry() {
			return new RegistryArgumentType();
		}

		public static Registry<?> getRegistry(CommandContext<ServerCommandSource> context, String name) {
			return context.getArgument(name, Registry.class);
		}

		@Override
		public Registry<?> parse(StringReader reader) throws CommandSyntaxException {
			var id = Identifier.fromCommandInput(reader);
			return Registry.REGISTRIES.getOrEmpty(id).orElseThrow(() -> UNKNOWN_REGISTRY_EXCEPTION.create(id));
		}

		@Override
		public Collection<String> getExamples() {
			return EXAMPLES;
		}

		@Override
		public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
			return CommandSource.suggestIdentifiers(Registry.REGISTRIES.getIds(), builder);
		}
	}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		ArgumentTypes.register("quilt:registry", RegistryArgumentType.class, new ConstantArgumentSerializer<>(RegistryArgumentType::registry));

		dispatcher.register(literal("dumpbuiltinregattrs")
				.then(argument("registry", RegistryArgumentType.registry())
						.requires(src -> src.hasPermissionLevel(4))
						.executes(DumpBuiltinAttributesCommand::execute))
		);
	}

	private static final Logger LOGGER = LogManager.getLogger("DumpBuiltinAttributesCommand");
	private static final SimpleCommandExceptionType IO_EXCEPTION =
			new SimpleCommandExceptionType(new LiteralText("IO exception occurred, check logs"));
	private static final SimpleCommandExceptionType ILLEGAL_STATE =
			new SimpleCommandExceptionType(new LiteralText("Encountered illegal state, check logs"));
	private static final SimpleCommandExceptionType ENCODE_FAILURE =
			new SimpleCommandExceptionType(new LiteralText("Failed to encode value, check logs"));
	private static final SimpleCommandExceptionType UNCAUGHT_EXCEPTION =
			new SimpleCommandExceptionType(new LiteralText("Uncaught exception occurred, check logs"));

	private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		var registry = RegistryArgumentType.getRegistry(ctx, "registry");
		try {
			execute0(ctx, registry);
		} catch (RuntimeException e) {
			LOGGER.error("Uncaught exception occurred", e);
			throw UNCAUGHT_EXCEPTION.create();
		}
		return SINGLE_SUCCESS;
	}

	@SuppressWarnings("unchecked")
	private static <R> void execute0(CommandContext<ServerCommandSource> ctx, Registry<R> registry) throws CommandSyntaxException {
		var registryId = registry.getKey().getValue();

		int attrCount = 0, valueCount = 0;

		var holder = RegistryEntryAttributeHolder.getBuiltin(registry);
		for (Map.Entry<? extends RegistryEntryAttribute<R, ?>, ? extends Map<R, Object>> entry : holder.valueTable.rowMap().entrySet()) {
			RegistryEntryAttribute<R, Object> attr = (RegistryEntryAttribute<R, Object>) entry.getKey();
			var attrId = attr.getId();

			var path = FabricLoader.getInstance().getGameDir().resolve("quilt/builtin-registry-entry-attributes")
					.resolve(attr.getSide().getSource().getDirectory())
					.resolve(attrId.getNamespace())
					.resolve("attributes")
					.resolve(registryId.getNamespace())
					.resolve(registryId.getPath())
					.normalize();
			if (!Files.exists(path)) {
				try {
					Files.createDirectories(path);
				} catch (IOException e) {
					LOGGER.error("Failed to create destination directory '" + path + "'", e);
					throw IO_EXCEPTION.create();
				}
			}

			path = path.resolve(attrId.getPath() + ".json");
			try {
				Files.deleteIfExists(path);
			} catch (IOException e) {
				LOGGER.error("Failed to delete destination file '" + path + "'", e);
				throw IO_EXCEPTION.create();
			}

			var valuesObj = new JsonObject();

			for (Map.Entry<R, Object> attrEntry : entry.getValue().entrySet()) {
				var entryId = registry.getId(attrEntry.getKey());
				if (entryId == null) {
					throw ILLEGAL_STATE.create();
				}
				DataResult<JsonElement> encodedValue =
						attr.getCodec().encodeStart(JsonOps.INSTANCE, attrEntry.getValue());
				if (encodedValue.result().isEmpty()) {
					if (encodedValue.error().isPresent()) {
						LOGGER.error("Failed to encode value for attribute {} of registry entry {}: {}",
								attr.getId(), entryId, encodedValue.error().get().message());
					} else {
						LOGGER.error("Failed to encode value for attribute {} of registry entry {}: unknown error",
								attr.getId(), entryId);
					}
					throw ENCODE_FAILURE.create();
				}

				valuesObj.add(entryId.toString(), encodedValue.result().get());
				valueCount++;
			}

			JsonObject obj = new JsonObject();
			obj.add("values", valuesObj);

			try (var writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(obj, writer);
			} catch (IOException e) {
				LOGGER.error("Failed to write JSON file '" + path + "'", e);
				throw IO_EXCEPTION.create();
			}

			attrCount++;
		}

		ctx.getSource().sendFeedback(new LiteralText("Done. Dumped " + attrCount + " attributes, " + valueCount + " values."),
				false);
	}
}
