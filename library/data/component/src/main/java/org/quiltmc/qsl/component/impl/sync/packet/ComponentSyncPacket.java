package org.quiltmc.qsl.component.impl.sync.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.Components;
import org.quiltmc.qsl.component.api.components.SyncedComponent;
import org.quiltmc.qsl.component.impl.client.sync.ClientSyncHandler;
import org.quiltmc.qsl.component.impl.sync.header.SyncPacketHeader;

import java.util.Map;

public class ComponentSyncPacket {
	@NotNull
	public static PacketByteBuf create(SyncPacketHeader<?> headerCreator, @NotNull ComponentProvider provider, Map<Identifier, SyncedComponent> components) {
		PacketByteBuf buff = headerCreator.start(provider);
		buff.writeVarInt(components.size());
		components.forEach((id, syncedComponent) -> {
			int rawId = Components.REGISTRY.getRawId(Components.REGISTRY.get(id));
			buff.writeInt(rawId);
			syncedComponent.writeToBuf(buff);
		});

		return buff;
	}

	public static void handle(PacketByteBuf buf) {
		int rawHeaderId = buf.readInt();
		var header = ClientSyncHandler.getInstance().getHeader(rawHeaderId);

		header.codec().decode(buf).ifPresent(provider -> {
			ComponentType<?> type = ClientSyncHandler.getInstance().getType(buf.readInt());

			provider.getContainer().receiveSyncPacket(type.id(), buf);
		});
	}
}
