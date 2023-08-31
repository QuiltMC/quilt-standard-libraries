/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.registry.impl.sync.mod_protocol;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.registry.impl.sync.ProtocolVersions;

public record ModProtocolDef(String id, String displayName, IntList versions, boolean optional) {
	public static void write(PacketByteBuf buf, ModProtocolDef def) {
		buf.writeString(def.id);
		buf.writeString(def.displayName);
		buf.writeIntList(def.versions);
		buf.writeBoolean(def.optional);
	}

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
