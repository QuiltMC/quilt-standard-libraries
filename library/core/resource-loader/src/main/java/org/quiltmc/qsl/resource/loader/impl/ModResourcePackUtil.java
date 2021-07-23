/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.resource.loader.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.google.common.base.Charsets;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.commons.io.IOUtils;

import net.minecraft.SharedConstants;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.resource.loader.api.GroupResourcePack;
import org.quiltmc.qsl.resource.loader.mixin.NamespaceResourceManagerAccessor;

public class ModResourcePackUtil {
	public static boolean containsDefault(ModMetadata info, String filename) {
		return "pack.mcmeta".equals(filename);
	}

	public static InputStream openDefault(ModMetadata info, ResourceType type, String filename) {
		if ("pack.mcmeta".equals(filename)) {
			String description = info.getName();

			if (description == null) {
				description = "";
			} else {
				description = description.replaceAll("\"", "\\\"");
			}

			var pack = String.format("""
							{"pack":{"pack_format":%d,"description":"%s"}}
							""",
					type.getPackVersion(SharedConstants.getGameVersion()), description);
			return IOUtils.toInputStream(pack, Charsets.UTF_8);
		}
		return null;
	}

	public static String getName(ModMetadata info) {
		if (info.getName() != null) {
			return info.getName();
		} else {
			return "Quilt Mod \"" + info.getId() + "\"";
		}
	}

	public void appendResources(NamespaceResourceManagerAccessor manager, GroupResourcePack groupResourcePack,
								Identifier id, List<Resource> resources) throws IOException {
		var packs = groupResourcePack.getPacks(id.getNamespace());

		if (packs == null) {
			return;
		}

		ResourceType type = manager.getType();
		Identifier metadataId = NamespaceResourceManagerAccessor.accessor_getMetadataPath(id);

		for (var pack : packs) {
			if (pack.contains(manager.getType(), id)) {
				InputStream metadataInputStream = pack.contains(type, metadataId) ? pack.open(type, metadataId) : null;
				resources.add(new ResourceImpl(pack.getName(), id, pack.open(type, id), metadataInputStream));
			}
		}
	}
}
