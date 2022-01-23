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

package org.quiltmc.qsl.command.api;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;

import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

public final class EnumArgumentType<E extends Enum<E>> implements ArgumentType<E> {
	public static final DynamicCommandExceptionType UNKNOWN_CONSTANT_NAME_EXCEPTION =
			new DynamicCommandExceptionType(o -> new TranslatableText("quilt.argument.enum.unknownConstantName", o));

	private static final Map<Class<? extends Enum<?>>, EnumArgumentType<?>> CACHE = new Reference2ReferenceLinkedOpenHashMap<>();

	private final Class<? extends E> clazz;
	private final E[] constants;
	private final String[] constantNames;

	private EnumArgumentType(Class<? extends E> clazz) {
		this.clazz = clazz;

		constants = clazz.getEnumConstants();
		if (constants == null) {
			throw new RuntimeException(clazz + " is not an enum (getEnumConstants() returned null)!");
		}

		constantNames = new String[constants.length];
		for (int i = 0; i < constants.length; i++) {
			constantNames[i] = constants[i].name();
		}
	}

	@SuppressWarnings("unchecked")
	public static <E extends Enum<E>> EnumArgumentType<E> enumConstant(Class<? extends E> enumClazz) {
		return (EnumArgumentType<E>) CACHE.computeIfAbsent(enumClazz, EnumArgumentType::new);
	}

	public static <E extends Enum<E>> E getEnumConstant(CommandContext<ServerCommandSource> context,
														String argumentName, Class<? extends E> enumClazz)
			throws CommandSyntaxException {
		return context.getArgument(argumentName, enumClazz);
	}

	@Override
	public E parse(StringReader reader) throws CommandSyntaxException {
		int cursor = reader.getCursor();
		String name = reader.getString();
		for (var constant : constants) {
			if (constant.name().equalsIgnoreCase(name)) {
				return constant;
			}
		}
		reader.setCursor(cursor);
		throw UNKNOWN_CONSTANT_NAME_EXCEPTION.createWithContext(reader, name);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return CommandSource.suggestMatching(constantNames, builder);
	}

	// FIXME this is a horrible, horrible idea...
	public static final class Serializer implements ArgumentSerializer<EnumArgumentType<?>> {
		@Override
		public void toPacket(EnumArgumentType<?> type, PacketByteBuf buf) {
			buf.writeString(type.clazz.getName());
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public EnumArgumentType<?> fromPacket(PacketByteBuf buf) {
			String className = buf.readString();
			Class<?> clazz;
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException("Failed to find enum class", e);
			}
			if (!clazz.isEnum()) {
				throw new RuntimeException("Class \"" + clazz + "\" is not an enum class");
			}
			return enumConstant((Class) clazz);
		}

		@Override
		public void toJson(EnumArgumentType<?> type, JsonObject json) {
			json.addProperty("className", type.clazz.getName());
		}
	}
}
