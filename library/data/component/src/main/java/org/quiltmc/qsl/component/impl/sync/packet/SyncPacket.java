package org.quiltmc.qsl.component.impl.sync.packet;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.api.components.SyncedComponent;
import org.quiltmc.qsl.component.impl.client.sync.ClientSyncHandler;
import org.quiltmc.qsl.component.impl.sync.codec.NetworkCodec;
import org.quiltmc.qsl.component.impl.sync.header.SyncPacketHeader;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncPacket {
	@NotNull
	public static PacketByteBuf create(SyncPacketHeader<?> headerCreator, @NotNull ComponentProvider provider, Map<Identifier, SyncedComponent> components) {
		PacketByteBuf buff = headerCreator.start(provider);
		buff.writeInt(components.size());
		components.forEach((id, syncedComponent) -> {
			NetworkCodec.COMPONENT_TYPE.encode(buff, Components.REGISTRY.get(id));
			syncedComponent.writeToBuf(buff);
		});

		return buff;
	}

	public static void handle(MinecraftClient client, PacketByteBuf buf) {
		var header = ClientSyncHandler.getInstance().getHeader(buf.readInt());
		header.codec().decode(buf).ifPresent(provider -> {
			var size = buf.readInt();

			for (int i = 0; i < size; i++) {
				NetworkCodec.COMPONENT_TYPE.decode(buf)
						.map(Components.REGISTRY::getId)
						.ifPresent(id -> provider.getContainer().receiveSyncPacket(id, buf));
			}
		});
	}

	public static void send(SyncContext context, ComponentProvider provider, HashMap<Identifier, SyncedComponent> map) {
		PacketByteBuf packet = create(context.header(), provider, map);

		context.playerGenerator().get().forEach(serverPlayer ->
				ServerPlayNetworking.send(serverPlayer, PacketIds.SYNC, packet)
		);
	}

	public record SyncContext(SyncPacketHeader<?> header, Supplier<Collection<ServerPlayerEntity>> playerGenerator) {
	}
}
