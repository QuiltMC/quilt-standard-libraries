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

package org.quiltmc.qsl.command.api;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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
import com.mojang.brigadier.tree.ArgumentCommandNode;

import net.minecraft.command.CommandBuildContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ArgumentTypeInfo;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;

/**
 * An {@link ArgumentType} that allows an arbitrary set of (case-insensitive) strings.
 * <p>
 * This argument type is compatible with Vanilla clients.
 */
public final class EnumArgumentType implements ArgumentType<String> {
	public static final DynamicCommandExceptionType UNKNOWN_VALUE_EXCEPTION =
			new DynamicCommandExceptionType(o -> Text.translatable("quilt.argument.enum.unknown_value", o));

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
	 * Gets the specified argument value from a command context.
	 *
	 * @param context      the command context
	 * @param argumentName the argument name
	 * @return the argument value
	 */
	public static String getEnum(CommandContext<?> context, String argumentName) {
		return context.getArgument(argumentName, String.class);
	}

	private static BiMap<Class<? extends Enum<?>>, EnumArgumentType> enumConstantTypes = null;

	/**
	 * Creates an {@code EnumArgumentType} based on the constants of the specified {@code enum} class.
	 *
	 * @param enumClass the enum class
	 * @param <E>       type of the enum class
	 * @return an argument type for the enum class
	 */
	public static <E extends Enum<E>> EnumArgumentType enumConstant(Class<? extends E> enumClass) {
		EnumArgumentType argType = null;

		if (enumConstantTypes == null) {
			enumConstantTypes = HashBiMap.create();
		} else {
			argType = enumConstantTypes.get(enumClass);
		}

		if (argType == null) {
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

			enumConstantTypes.put(enumClass, argType = new EnumArgumentType(values));
		}

		return argType;
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
	public static <E extends Enum<E>> E getEnumConstant(CommandContext<?> context,
			String argumentName, Class<? extends E> enumClass)
			throws CommandSyntaxException {
		if (enumConstantTypes == null) {
			throw new IllegalArgumentException(enumClass + " does not have an associated EnumArgumentType");
		}

		boolean found = false;

		for (var node : context.getNodes()) {
			if (node.getNode().getName().equals(argumentName)) {
				var argChildNode = node.getNode();

				if (argChildNode instanceof ArgumentCommandNode<?, ?> argNode) {
					if (argNode.getType() instanceof EnumArgumentType enumConstantType) {
						var expectedClass = enumConstantTypes.inverse().get(enumConstantType);
						if (expectedClass == null) {
							throw new IllegalArgumentException(argumentName + "'s type does not have an associated enum class");
						} else if (expectedClass != enumClass) {
							throw new IllegalArgumentException(argumentName + "'s type is derived from  " + expectedClass
									+ ", not from " + enumClass);
						}

						found = true;
						break;
					} else {
						throw new IllegalArgumentException(argumentName + " is not of EnumArgumentType");
					}
				} else {
					throw new IllegalArgumentException("Command does not have an argument named " + argumentName);
				}
			}
		}

		if (!found) {
			throw new IllegalStateException("Analysis of command nodes failed to find and check for argument " + argumentName);
		}

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

	public static final class Info implements ArgumentTypeInfo<EnumArgumentType, Info.Template> {
		@Override
		public void serializeToNetwork(Template type, PacketByteBuf buf) {
			buf.writeCollection(type.values, PacketByteBuf::writeString);
		}

		@Override
		public Template deserializeFromNetwork(PacketByteBuf buf) {
			Set<String> values = buf.readCollection(LinkedHashSet::new, PacketByteBuf::readString);
			return new Template(values);
		}

		@Override
		public void serializeToJson(Template type, JsonObject json) {
			var valuesArr = new JsonArray();

			for (var value : type.values) {
				valuesArr.add(value);
			}

			json.add("values", valuesArr);
		}

		@Override
		public Template unpack(EnumArgumentType type) {
			return new Template(type.values);
		}

		public final class Template implements ArgumentTypeInfo.Template<EnumArgumentType> {
			private final Set<String> values;

			public Template(Set<String> values) {
				this.values = values;
			}

			@Override
			public EnumArgumentType instantiate(CommandBuildContext context) {
				return new EnumArgumentType(this.values);
			}

			@Override
			public ArgumentTypeInfo<EnumArgumentType, ?> type() {
				return Info.this;
			}
		}
	}
}
