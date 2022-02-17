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

/**
 * An {@link ArgumentType} that allows an arbitrary set of (case-insensitive) strings.
 *
 * <p>Using this argument type will <em>not</em> prevent Vanilla clients from joining your server.
 */
public final class EnumArgumentType implements ArgumentType<String> {
	public static final DynamicCommandExceptionType UNKNOWN_VALUE_EXCEPTION =
			new DynamicCommandExceptionType(o -> new TranslatableText("quilt.argument.enum.unknown_value", o));

	private final Set<String> values;

	/**
	 * Creates a new {@code EnumArgumentType}.
	 *
	 * @param values the value set
	 */
	public EnumArgumentType(String... values) {
		this.values = new LinkedHashSet<>(values.length);

		for (String value : values) {
			var valueLC = value.toLowerCase(Locale.ROOT);

			if (!this.values.add(valueLC)) {
				throw new IllegalArgumentException("Duplicate value \"%s\" (after converting to lowercase)".formatted(valueLC));
			}
		}
	}

	// doesn't copy set, assumes all strings are already lowercase
	private EnumArgumentType(Set<String> values) {
		this.values = values;
	}

	/**
	 * Creates an {@code EnumArgumentType} based on the constants of the specified {@code enum} class.
	 *
	 * @param enumClass the enum class
	 * @param <E>       type of the enum class
	 * @return an argument type for the enum class
	 */
	public static <E extends Enum<E>> EnumArgumentType enumConstant(Class<? extends E> enumClass) {
		E[] constants = enumClass.getEnumConstants();

		if (constants == null) {
			throw new IllegalArgumentException("%s is not an enum class (getEnumConstants() returned null)".formatted(enumClass));
		}

		var values = new LinkedHashSet<String>(constants.length);

		for (E constant : constants) {
			var constNameLC = constant.name().toLowerCase(Locale.ROOT);

			if (!values.add(constNameLC)) {
				throw new IllegalArgumentException(("%s contains 2 constants with the same name after converting to lowercase " +
						"(\"%s\")").formatted(enumClass, constNameLC));
			}
		}

		return new EnumArgumentType(values);
	}

	/**
	 * Gets the specified argument value from a command context.
	 *
	 * @param context      the command context
	 * @param argumentName the argument name
	 * @return the argument value
	 */
	public static String getEnum(CommandContext<ServerCommandSource> context,
	                             String argumentName) {
		return context.getArgument(argumentName, String.class);
	}

	/**
	 * Gets the specified argument from a command context, mapped to its matching {@code enum} constant.
	 *
	 * @param context      the command context
	 * @param argumentName the argument name
	 * @param enumClass    the enum class to map to
	 * @param <E>          the type of enum class
	 * @return the argument as an {@code enum} constant
	 * @throws CommandSyntaxException if the argument doesn't match a known enum constant
	 */
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
		String value = reader.readUnquotedString().toLowerCase(Locale.ROOT);

		if (this.values.contains(value)) {
			return value;
		}

		reader.setCursor(cursor);
		throw UNKNOWN_VALUE_EXCEPTION.createWithContext(reader, value);
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		return CommandSource.suggestMatching(this.values, builder);
	}

	@Override
	public Collection<String> getExamples() {
		return StringArgumentType.StringType.SINGLE_WORD.getExamples();
	}

	public static final class Serializer implements ArgumentSerializer<EnumArgumentType> {
		@Override
		public void toPacket(EnumArgumentType type, PacketByteBuf buf) {
			buf.writeCollection(type.values, PacketByteBuf::writeString);
		}

		@Override
		public EnumArgumentType fromPacket(PacketByteBuf buf) {
			Set<String> values = buf.readCollection(LinkedHashSet::new, PacketByteBuf::readString);
			return new EnumArgumentType(values);
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
