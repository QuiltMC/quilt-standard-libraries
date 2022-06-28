package org.quiltmc.qsl.component.impl.client.sync;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IdList;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.networking.api.PacketByteBufs;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class ClientRegistryPacket {

	@NotNull
	public static <T> CompletableFuture<PacketByteBuf> handleRegistryPacket(@NotNull PacketByteBuf buf, Registry<T> registry, Consumer<IdList<T>> action) {
		return CompletableFuture.supplyAsync(() -> {
			var finalEither = createIdList(buf, registry)
					.ifLeft(action)
					.mapLeft(clientHeaderList -> PacketByteBufs.create().writeString("Ok"))
					.mapRight(PacketByteBufs.create()::writeIdentifier);

			return finalEither.left().isPresent() ? finalEither.left().get() : finalEither.right().get();
		});
	}

	@NotNull
	public static <T> Either<IdList<T>, Identifier> createIdList(PacketByteBuf buf, Registry<T> targetRegistry) {
		int size = buf.readInt();
		IdList<T> ret = new IdList<>(size); // size is dropped

		for (int i = 0; i < size; i++) {
			Identifier id = buf.readIdentifier();
			int rawId = buf.readInt(); // a whole entry is dropped

			Optional<T> type = targetRegistry.getOrEmpty(id);
			if (type.isEmpty()) {
				return Either.right(id);
			}

			ret.set(type.get(), rawId);
		}

		return Either.left(ret);
	}
}
