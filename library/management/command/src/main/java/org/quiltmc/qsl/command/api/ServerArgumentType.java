/*
 * Copyright 2016, 2017, 2018, 2019, 2020 zml and Colonel contributors
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

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.command.impl.ServerArgumentTypeImpl;
import org.quiltmc.qsl.command.impl.ServerArgumentTypes;

/**
 * Represents an argument type that only needs to be known server-side.
 *
 * @param <T> the argument type
 */
@ApiStatus.NonExtendable
public interface ServerArgumentType<T extends ArgumentType<?>> {
	/**
	 * Gets the identifier of this argument type.
	 *
	 * @return argument type identifier
	 */
	Identifier id();

	/**
	 * Gets the class of this argument type.
	 *
	 * @return argument type class
	 */
	Class<? extends T> type();

	/**
	 * Gets the serializer of this argument type.
	 * <p>
	 * This will only be used on clients who recognize this argument.
	 *
	 * @return argument serializer
	 */
	ArgumentSerializer<T> serializer();

	/**
	 * Gets the fallback provider of this argument type.
	 *
	 * @return argument fallback provider
	 */
	ArgumentTypeFallbackProvider<T> fallbackProvider();

	/**
	 * Gets the fallback suggestion provider of this argument type.
	 *
	 * @return argument fallback suggestion provider
	 */
	@Nullable SuggestionProvider<?> fallbackSuggestions();

	/**
	 * Creates and registers a new server-side argument type.
	 *
	 * @param id                  the argument type's identifier
	 * @param type                the argument type class
	 * @param serializer          the argument serializer
	 * @param fallbackProvider    the fallback provider
	 * @param fallbackSuggestions the fallback suggestion provider
	 * @param <T>                 the argument type
	 * @return a newly-created server-side argument type
	 */
	@SuppressWarnings("unchecked")
	static <T extends ArgumentType<?>> ServerArgumentType<T> register(Identifier id, Class<? extends T> type,
	                                                                  ArgumentSerializer<T> serializer,
	                                                                  ArgumentTypeFallbackProvider<T> fallbackProvider,
	                                                                  @Nullable SuggestionProvider<?> fallbackSuggestions) {
		var value = new ServerArgumentTypeImpl<>(id, type, serializer, fallbackProvider, fallbackSuggestions);
		ArgumentTypes.register(id.toString(), (Class<T>) type, serializer);
		ServerArgumentTypes.register(value);
		return value;
	}

	/**
	 * Creates and registers a new server-side argument type.
	 *
	 * @param id               the argument type's identifier
	 * @param type             the argument type class
	 * @param serializer       the argument serializer
	 * @param fallbackProvider the fallback provider
	 * @param <T>              the argument type
	 * @return a newly-created server-side argument type
	 */
	static <T extends ArgumentType<?>> ServerArgumentType<T> register(Identifier id, Class<? extends T> type,
	                                                                  ArgumentSerializer<T> serializer,
	                                                                  ArgumentTypeFallbackProvider<T> fallbackProvider) {
		return register(id, type, serializer, fallbackProvider, SuggestionProviders.ASK_SERVER);
	}
}
