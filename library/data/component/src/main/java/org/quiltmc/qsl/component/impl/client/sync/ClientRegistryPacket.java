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

package org.quiltmc.qsl.component.impl.client.sync;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.mojang.datafixers.util.Either;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.networking.api.PacketByteBufs;

@Environment(EnvType.CLIENT)
public final class ClientRegistryPacket {
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	public static <T> CompletableFuture<PacketByteBuf> handleRegistryPacket(PacketByteBuf buf, Registry<T> registry, Consumer<IdList<T>> action) {
		return CompletableFuture.supplyAsync(() -> {
			var finalEither = createIdList(buf, registry)
					.ifLeft(action)
					.mapLeft(clientHeaderList -> PacketByteBufs.create().writeString("Ok"))
					.mapRight(PacketByteBufs.create()::writeIdentifier);

			return finalEither.left().isPresent() ? finalEither.left().get() : finalEither.right().get(); // if we don't have a left value we will definitely have a right one
		});
	}

	public static <T> Either<IdList<T>, Identifier> createIdList(PacketByteBuf buf, Registry<T> targetRegistry) {
		int size = buf.readInt();
		IdList<T> ret = new IdList<>(size); // size is dropped

		for (int i = 0; i < size; i++) {
			Identifier id = buf.readIdentifier(); // consume identifier
			int rawId = buf.readVarInt(); // consumer rawId

			Optional<T> type = targetRegistry.getOrEmpty(id);
			if (type.isEmpty()) {
				return Either.right(id);
			}

			ret.set(type.get(), rawId);
		}

		return Either.left(ret);
	}
}
