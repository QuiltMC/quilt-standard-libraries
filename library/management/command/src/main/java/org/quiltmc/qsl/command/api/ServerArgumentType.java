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

import net.minecraft.command.argument.ArgumentTypeInfo;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;

import org.quiltmc.qsl.command.impl.ServerArgumentTypeImpl;
import org.quiltmc.qsl.command.impl.ServerArgumentTypes;
import org.quiltmc.qsl.command.mixin.ArgumentTypeInfosAccessor;
import org.quiltmc.qsl.registry.api.sync.RegistrySynchronization;

/**
 * Represents an argument type that only needs to be known server-side.
 *
 * @param <A> the argument type
 */
@ApiStatus.NonExtendable
public interface ServerArgumentType<A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> {
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
	Class<? extends A> type();

	/**
	 * Gets the information of this argument type.
	 * <p>
	 * This will only be used on clients who recognize this argument.
	 *
	 * @return argument type information
	 */
	ArgumentTypeInfo<A, T> typeInfo();

	/**
	 * Gets the fallback provider of this argument type.
	 *
	 * @return argument fallback provider
	 */
	ArgumentTypeFallbackProvider<A> fallbackProvider();

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
	 * @param typeInfo            the argument type info
	 * @param fallbackProvider    the fallback provider
	 * @param fallbackSuggestions the fallback suggestion provider
	 * @param <A>                 the argument type
	 * @return a newly-created server-side argument type
	 */
	static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ServerArgumentType<A, T> register(
			Identifier id, Class<? extends A> type, ArgumentTypeInfo<A, T> typeInfo,
			ArgumentTypeFallbackProvider<A> fallbackProvider, @Nullable SuggestionProvider<?> fallbackSuggestions) {
		var value = new ServerArgumentTypeImpl<>(id, type, typeInfo, fallbackProvider, fallbackSuggestions);
		var info = ArgumentTypeInfosAccessor.callRegister(Registry.COMMAND_ARGUMENT_TYPE, id.toString(), type, typeInfo);
		RegistrySynchronization.setEntryOptional((SimpleRegistry<ArgumentTypeInfo<?, ?>>) Registry.COMMAND_ARGUMENT_TYPE, info);
		ServerArgumentTypes.register(value);
		return value;
	}

	/**
	 * Creates and registers a new server-side argument type.
	 *
	 * @param id               the argument type's identifier
	 * @param type             the argument type info
	 * @param typeInfo         the argument serializer
	 * @param fallbackProvider the fallback provider
	 * @param <A>              the argument type
	 * @return a newly-created server-side argument type
	 */
	static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ServerArgumentType<A, T> register(
			Identifier id, Class<? extends A> type, ArgumentTypeInfo<A, T> typeInfo,
			ArgumentTypeFallbackProvider<A> fallbackProvider
	) {
		return register(id, type, typeInfo, fallbackProvider, SuggestionProviders.ASK_SERVER);
	}
}
