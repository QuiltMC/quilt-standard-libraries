/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.registry.attachment.impl;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;

@ApiStatus.Internal
public final class DumpBuiltinAttachmentsCommand {
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, integrated, dedicated) -> register0(dispatcher));
	}

	private static void register0(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("dump_builtin_attachments")
				.then(argument("registry", IdentifierArgumentType.identifier())
						.requires(src -> src.hasPermissionLevel(4))
						.executes(DumpBuiltinAttachmentsCommand::execute))
		);
	}

	private static final Logger LOGGER = LogUtils.getLogger();

	private static final DynamicCommandExceptionType UNKNOWN_REGISTRY_EXCEPTION = new DynamicCommandExceptionType(
			o -> Text.literal("Could not find registry " + o));
	private static final SimpleCommandExceptionType IO_EXCEPTION =
			new SimpleCommandExceptionType(Text.literal("IO exception occurred, check logs"));
	private static final SimpleCommandExceptionType ILLEGAL_STATE =
			new SimpleCommandExceptionType(Text.literal("Encountered illegal state, check logs"));
	private static final SimpleCommandExceptionType ENCODE_FAILURE =
			new SimpleCommandExceptionType(Text.literal("Failed to encode value, check logs"));
	private static final SimpleCommandExceptionType UNCAUGHT_EXCEPTION =
			new SimpleCommandExceptionType(Text.literal("Uncaught exception occurred, check logs"));

	private static int execute(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
		var registryId = IdentifierArgumentType.getIdentifier(ctx, "registry");
		var registry = Registries.REGISTRY.get(registryId);
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

		int attachmentCount = 0, valueCount = 0;

		var holder = RegistryEntryAttachmentHolder.getBuiltin(registry);
		for (Map.Entry<? extends RegistryEntryAttachment<R, ?>, ? extends Map<R, Object>> entry : holder.valueTable.rowMap().entrySet()) {
			RegistryEntryAttachment<R, Object> attachment = (RegistryEntryAttachment<R, Object>) entry.getKey();
			var attachmentId = attachment.id();

			if (!ClientSideGuard.isAccessAllowed() && attachment.side() == RegistryEntryAttachment.Side.CLIENT) {
				continue;
			}

			var path = QuiltLoader.getGameDir().resolve("quilt/builtin_registry_attachments")
					.resolve(attachment.side().getSource().getDirectory())
					.resolve(attachmentId.getNamespace())
					.resolve("attachments")
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

			path = path.resolve(attachmentId.getPath() + ".json");
			try {
				Files.deleteIfExists(path);
			} catch (IOException e) {
				LOGGER.error("Failed to delete destination file '" + path + "'", e);
				throw IO_EXCEPTION.create();
			}

			var valuesObj = new JsonObject();

			// do tags first
			for (Map.Entry<TagKey<R>, Object> attachmentEntry : holder.valueTagTable.row(attachment).entrySet()) {
				var entryId = attachmentEntry.getKey().id();
				DataResult<JsonElement> encodedValue =
						attachment.codec().encodeStart(JsonOps.INSTANCE, attachmentEntry.getValue());
				if (encodedValue.result().isEmpty()) {
					if (encodedValue.error().isPresent()) {
						LOGGER.error("Failed to encode value for attachment #{} of registry entry {}: {}",
								attachment.id(), entryId, encodedValue.error().get().message());
					} else {
						LOGGER.error("Failed to encode value for attachment #{} of registry entry {}: unknown error",
								attachment.id(), entryId);
					}

					throw ENCODE_FAILURE.create();
				}

				valuesObj.add("#" + entryId, encodedValue.result().get());
				valueCount++;
			}

			for (Map.Entry<R, Object> attachmentEntry : entry.getValue().entrySet()) {
				if (holder.isValueComputed(attachment, attachmentEntry.getKey())) {
					continue;
				}

				var entryId = registry.getId(attachmentEntry.getKey());
				if (entryId == null) {
					throw ILLEGAL_STATE.create();
				}

				DataResult<JsonElement> encodedValue =
						attachment.codec().encodeStart(JsonOps.INSTANCE, attachmentEntry.getValue());
				if (encodedValue.result().isEmpty()) {
					if (encodedValue.error().isPresent()) {
						LOGGER.error("Failed to encode value for attachment {} of registry entry {}: {}",
								attachment.id(), entryId, encodedValue.error().get().message());
					} else {
						LOGGER.error("Failed to encode value for attachment {} of registry entry {}: unknown error",
								attachment.id(), entryId);
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

			attachmentCount++;
		}

		ctx.getSource().sendSystemMessage(Text.literal("Done. Dumped " + attachmentCount + " attachments, " + valueCount + " values."));
	}
}
