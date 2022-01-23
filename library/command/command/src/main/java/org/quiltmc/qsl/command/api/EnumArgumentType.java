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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;

public final class EnumArgumentType implements ArgumentType<String> {
	public static final DynamicCommandExceptionType UNKNOWN_VALUE_EXCEPTION =
			new DynamicCommandExceptionType(o -> new TranslatableText("quilt.argument.enum.unknownConstantName", o));

	private final Set<String> values;

	public EnumArgumentType(String... values) {
		this.values = new LinkedHashSet<>(values.length);
		for (String value : values) {
			this.values.add(value.toLowerCase(Locale.ROOT));
		}
	}

	// doesn't copy set, assumes all strings are already lowercase
	private EnumArgumentType(Set<String> values, boolean dummy) {
		this.values = values;
	}

	public static <E extends Enum<E>> EnumArgumentType enumConstant(Class<? extends E> enumClass) {
		E[] constants = enumClass.getEnumConstants();
		if (constants == null) {
			throw new IllegalArgumentException(enumClass + " is not an enum class (getEnumConstants() returned null)");
		}
		Set<String> values = new LinkedHashSet<>(constants.length);
		for (E constant : constants) {
			values.add(constant.name().toLowerCase(Locale.ROOT));
		}
		return new EnumArgumentType(values, false);
	}

	public static <E extends Enum<E>> E getEnumConstant(CommandContext<ServerCommandSource> context,
														String argumentName, Class<? extends E> enumClass)
			throws CommandSyntaxException {
		String value = context.getArgument(argumentName, String.class);
		E[] constants = enumClass.getEnumConstants();
		if (constants == null) {
			throw new IllegalArgumentException(enumClass + " is not an enum class (getEnumConstants() returned null)");
		}
		for (var constant : constants) {
			if (constant.name().equalsIgnoreCase(value)) {
				return constant;
			}
		}
		throw UNKNOWN_VALUE_EXCEPTION.create(argumentName);
	}

	@Override
	public String parse(StringReader reader) throws CommandSyntaxException {
		int cursor = reader.getCursor();
		String value = reader.getString().toLowerCase(Locale.ROOT);
		if (values.contains(value)) {
			return value;
		}
		reader.setCursor(cursor);
		throw UNKNOWN_VALUE_EXCEPTION.createWithContext(reader, value);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return CommandSource.suggestMatching(values, builder);
	}

	@Override
	public Collection<String> getExamples() {
		return StringArgumentType.StringType.SINGLE_WORD.getExamples();
	}

	public static final class Serializer implements ArgumentSerializer<EnumArgumentType> {
		@Override
		public void toPacket(EnumArgumentType type, PacketByteBuf buf) {
			buf.writeVarInt(type.values.size());
			for (var value : type.values) {
				buf.writeString(value);
			}
		}

		@Override
		public EnumArgumentType fromPacket(PacketByteBuf buf) {
			int size = buf.readVarInt();
			Set<String> values = new LinkedHashSet<>(size);
			for (int i = 0; i < size; i++) {
				// note: no lowercase conversion here, since values are already written in lowercase
				values.add(buf.readString());
			}
			return new EnumArgumentType(values, false);
		}

		@Override
		public void toJson(EnumArgumentType type, JsonObject json) {
			var valuesArr = new JsonArray();
			for (var value : type.values) {
				valuesArr.add(value);
			}
			json.add("values", valuesArr);
		}
	}
}
