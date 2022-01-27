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

package org.quiltmc.qsl.registry.dict.impl;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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

import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.registry.dict.api.RegistryDict;

@ApiStatus.Internal
public final class DumpBuiltinDictsCommand {
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, integrated, dedicated) -> register0(dispatcher));
	}

	private static void register0(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("dumpbuiltindicts")
				.then(argument("registry", IdentifierArgumentType.identifier())
						.requires(src -> src.hasPermissionLevel(4))
						.executes(DumpBuiltinDictsCommand::execute))
		);
	}

	private static final Logger LOGGER = LogManager.getLogger("DumpBuiltinDictsCommand");

	private static final DynamicCommandExceptionType UNKNOWN_REGISTRY_EXCEPTION = new DynamicCommandExceptionType(
			o -> new LiteralText("Could not find registry " + o));
	private static final SimpleCommandExceptionType IO_EXCEPTION =
			new SimpleCommandExceptionType(new LiteralText("IO exception occurred, check logs"));
	private static final SimpleCommandExceptionType ILLEGAL_STATE =
			new SimpleCommandExceptionType(new LiteralText("Encountered illegal state, check logs"));
	private static final SimpleCommandExceptionType ENCODE_FAILURE =
			new SimpleCommandExceptionType(new LiteralText("Failed to encode value, check logs"));
	private static final SimpleCommandExceptionType UNCAUGHT_EXCEPTION =
			new SimpleCommandExceptionType(new LiteralText("Uncaught exception occurred, check logs"));

	private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		var registryId = IdentifierArgumentType.getIdentifier(ctx, "registry");
		var registry = Registry.REGISTRIES.get(registryId);
		if (registry == null) {
			throw UNKNOWN_REGISTRY_EXCEPTION.create(registryId);
		}
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

		int dictCount = 0, valueCount = 0;

		var holder = RegistryDictHolder.getBuiltin(registry);
		for (Map.Entry<? extends RegistryDict<R, ?>, ? extends Map<R, Object>> entry : holder.valueTable.rowMap().entrySet()) {
			RegistryDict<R, Object> dict = (RegistryDict<R, Object>) entry.getKey();
			var dictId = dict.id();

			if (!AssetsHolderGuard.isAccessAllowed() && dict.side() == RegistryDict.Side.CLIENT) {
				continue;
			}

			var path = FabricLoader.getInstance().getGameDir().resolve("quilt/builtin-registry-dictionaries")
					.resolve(dict.side().getSource().getDirectory())
					.resolve(dictId.getNamespace())
					.resolve("dicts")
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

			path = path.resolve(dictId.getPath() + ".json");
			try {
				Files.deleteIfExists(path);
			} catch (IOException e) {
				LOGGER.error("Failed to delete destination file '" + path + "'", e);
				throw IO_EXCEPTION.create();
			}

			var valuesObj = new JsonObject();

			for (Map.Entry<R, Object> dictEntry : entry.getValue().entrySet()) {
				var entryId = registry.getId(dictEntry.getKey());
				if (entryId == null) {
					throw ILLEGAL_STATE.create();
				}
				DataResult<JsonElement> encodedValue =
						dict.codec().encodeStart(JsonOps.INSTANCE, dictEntry.getValue());
				if (encodedValue.result().isEmpty()) {
					if (encodedValue.error().isPresent()) {
						LOGGER.error("Failed to encode value for dictionary {} of registry entry {}: {}",
								dict.id(), entryId, encodedValue.error().get().message());
					} else {
						LOGGER.error("Failed to encode value for dictionary {} of registry entry {}: unknown error",
								dict.id(), entryId);
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

			dictCount++;
		}

		ctx.getSource().sendFeedback(new LiteralText("Done. Dumped " + dictCount + " dictionaries, " + valueCount + " values."),
				false);
	}
}
