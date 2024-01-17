/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2021 The Quilt Project
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

import java.io.InputStream;
import java.util.ArrayList;

import com.google.common.base.Charsets;
import com.mojang.logging.LogUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.SharedConstants;
import net.minecraft.resource.ResourceIoSupplier;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.DataPackSettings;
import net.minecraft.resource.pack.PackProfile;
import net.minecraft.resource.pack.ResourcePack;

import org.quiltmc.loader.api.ModMetadata;
import org.quiltmc.qsl.resource.loader.api.QuiltPackProfile;
import org.quiltmc.qsl.resource.loader.api.PackActivationType;

@ApiStatus.Internal
public final class ModPackUtil {
	/**
	 * Represents the default data-pack settings, including the default-enabled built-in data-packs.
	 */
	public static final DataPackSettings DEFAULT_SETTINGS = createDefaultDataPackSettings(DataPackSettings.SAFE_MODE);

	private static final Logger LOGGER = LogUtils.getLogger();

	public static String getPackMeta(@Nullable String description, ResourceType type) {
		if (description == null) {
			description = "";
		} else {
			description = description.replace("\\", "\\u005C");
			description = description.replace("\"", "\\u0022");
		}

		return String.format("""
						{"pack":{"pack_format":%d,"description":"%s"}}
						""",
				SharedConstants.getGameVersion().getResourceVersion(type), description);
	}

	public static @Nullable ResourceIoSupplier<InputStream> openDefault(ModMetadata info, ResourceType type, String filename) {
		if ("pack.mcmeta".equals(filename)) {
			var pack = getPackMeta(info.name(), type);
			return () -> IOUtils.toInputStream(pack, Charsets.UTF_8);
		}

		return null;
	}

	public static String getName(ModMetadata info) {
		if (info.name() != null) {
			return info.name();
		} else {
			return "Quilt Mod \"" + info.id() + "\"";
		}
	}

	public static DataPackSettings createDefaultDataPackSettings(DataPackSettings source) {
		var moddedResourcePacks = new ArrayList<PackProfile>();
		ModPackProvider.SERVER_RESOURCE_PACK_PROVIDER.register(moddedResourcePacks::add);

		var enabled = new ArrayList<>(source.getEnabled());
		var disabled = new ArrayList<>(source.getDisabled());

		// This ensure that any built-in registered data packs by mods which needs to be enabled by default are
		// as the data pack screen automatically put any data pack as disabled except the Default data pack.
		for (var profile : moddedResourcePacks) {
			ResourcePack pack = profile.createPack();

			if (pack.getActivationType().isEnabledByDefault()) {
				enabled.add(profile.getName());
			} else {
				disabled.add(profile.getName());
			}
		}

		return new DataPackSettings(enabled, disabled);
	}

	public static PackProfile makeBuiltinPackProfile(ModNioPack pack, PackProfile.Info info) {
		return PackProfile.of(
			pack.getName(),
			pack.getDisplayName(),
                pack.getActivationType() == PackActivationType.ALWAYS_ENABLED,
			QuiltPackProfile.wrapToFactory(pack),
			info,
			PackProfile.InsertionPosition.TOP,
			false,
			new BuiltinResourcePackSource(pack)
		);
	}

	static @Nullable PackProfile makeBuiltinPackProfile(ModNioPack pack) {
		// I think the resource version really shouldn't matter here, but we'll go for the latest asset version just in case
		PackProfile.Info info = PackProfile.readInfoFromPack(pack.getName(), QuiltPackProfile.wrapToFactory(pack),
				SharedConstants.getGameVersion().getResourceVersion(ResourceType.CLIENT_RESOURCES));

		if (info == null) {
			LOGGER.warn("Couldn't find pack meta for pack {}.", pack.getName());
			return null;
		}

		return makeBuiltinPackProfile(pack, info);
	}
}
