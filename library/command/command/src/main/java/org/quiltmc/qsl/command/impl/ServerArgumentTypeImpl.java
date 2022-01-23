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

package org.quiltmc.qsl.command.impl;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.command.api.ArgumentTypeFallbackProvider;
import org.quiltmc.qsl.command.api.ServerArgumentType;

@ApiStatus.Internal
public record ServerArgumentTypeImpl<T extends ArgumentType<?>>(Identifier id, Class<? extends T> type,
																ArgumentSerializer<T> serializer,
																ArgumentTypeFallbackProvider<T> fallbackProvider,
																@Nullable SuggestionProvider<?> fallbackSuggestions)
		implements ServerArgumentType<T> {
	@Override
	public String toString() {
		return "ServerArgumentType{" +
				"id=" + id +
				", type=" + type +
				'}';
	}
}
