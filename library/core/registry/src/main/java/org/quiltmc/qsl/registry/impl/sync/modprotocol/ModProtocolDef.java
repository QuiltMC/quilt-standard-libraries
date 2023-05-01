package org.quiltmc.qsl.registry.impl.sync.modprotocol;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.PacketByteBuf;
import org.quiltmc.qsl.registry.impl.sync.ProtocolVersions;

public record ModProtocolDef(String id, String displayName, IntList versions, boolean optional) {
	public static void write(PacketByteBuf buf, ModProtocolDef def) {
		buf.writeString(def.id);
		buf.writeString(def.displayName);
		buf.writeIntList(def.versions);
		buf.writeBoolean(def.optional);	}

	public static ModProtocolDef read(PacketByteBuf buf) {
		var id = buf.readString();
		var name = buf.readString();
		var versions = buf.readIntList();
		var optional = buf.readBoolean();
		return new ModProtocolDef(id, name, versions, optional);
	}

	public int latestMatchingVersion(IntCollection versions) {
		return ProtocolVersions.getHighestSupported(versions, this.versions);
	}
}
