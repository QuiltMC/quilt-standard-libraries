/*
 * Copyright 2016, 2017, 2018, 2019, 2020 zml and Colonel contributors
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

package org.quiltmc.qsl.command.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.brigadier.arguments.ArgumentType;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.command.argument.ArgumentTypeInfo;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.command.api.ServerArgumentType;

@ApiStatus.Internal
public final class ServerArgumentTypes {
	private ServerArgumentTypes() {
	}

	private static final Map<Class<?>, ServerArgumentType<?, ?>> BY_TYPE = new Reference2ObjectOpenHashMap<>();
	private static final Map<Identifier, ServerArgumentType<?, ?>> BY_ID = new ConcurrentHashMap<>();

	@SuppressWarnings("unchecked")
	public static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ServerArgumentType<A, T> byClass(Class<A> clazz) {
		return (ServerArgumentType<A, T>) BY_TYPE.get(clazz);
	}

	public static void register(ServerArgumentType<?, ?> type) {
		BY_TYPE.put(type.type(), type);
		BY_ID.put(type.id(), type);
	}

	public static Set<Identifier> getIds() {
		return Collections.unmodifiableSet(BY_ID.keySet());
	}

	public static void setKnownArgumentTypes(ServerLoginNetworkHandler handler, Set<Identifier> types) {
		((KnownArgTypesStorage) handler).quilt$setKnownArgumentTypes(types);
	}

	public static Set<Identifier> getKnownArgumentTypes(ServerPlayerEntity player) {
		return ((KnownArgTypesStorage) player).quilt$getKnownArgumentTypes();
	}
}
