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

package org.quiltmc.qsl.resource.loader.impl;

import java.io.IOException;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import net.minecraft.resource.pack.ResourcePackCompatibility;
import net.minecraft.resource.pack.ResourcePackProfile;
import net.minecraft.resource.pack.ResourcePackSource;
import net.minecraft.resource.pack.metadata.PackResourceMetadata;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import org.quiltmc.qsl.resource.loader.api.ResourcePackActivationType;

@ApiStatus.Internal
public final class QuiltBuiltinResourcePackProfile extends ResourcePackProfile {
	private static final Logger LOGGER = LogUtils.getLogger();

	static QuiltBuiltinResourcePackProfile of(ModNioResourcePack pack) {
		try {
			PackResourceMetadata metadata = pack.parseMetadata(PackResourceMetadata.READER);
			if (metadata == null) {
				LOGGER.warn("Couldn't find pack meta for pack {}", pack.getName());
				return null;
			}

			return new QuiltBuiltinResourcePackProfile(pack, metadata);
		} catch (IOException e) {
			LOGGER.warn("Couldn't get pack info for: {}", e.toString());
			return null;
		}
	}

	private QuiltBuiltinResourcePackProfile(ModNioResourcePack pack, PackResourceMetadata metadata) {
		super(
				pack.getName(),
				pack.getDisplayName(),
				pack.getActivationType() == ResourcePackActivationType.ALWAYS_ENABLED,
				() -> pack,
				metadata,
				pack.type,
				ResourcePackProfile.InsertionPosition.TOP,
				new BuiltinResourcePackSource(pack)
		);
	}

	@Override
	public ResourcePackCompatibility getCompatibility() {
		// This is to ease multi-version mods whose built-in packs actually work across versions.
		return ResourcePackCompatibility.COMPATIBLE;
	}

	/**
	 * Represents a built-in resource pack source.
	 * Similar to {@link ResourcePackSource#PACK_SOURCE_BUILTIN} but specifies the mod name too.
	 */
	public static class BuiltinResourcePackSource implements ResourcePackSource {
		private static final Text SOURCE_BUILTIN_TEXT = new TranslatableText("pack.source.builtin");
		private final ModNioResourcePack pack;
		private final Text text;
		private final Text tooltip;

		BuiltinResourcePackSource(ModNioResourcePack pack) {
			String modName = pack.modInfo.name();

			if (modName == null) {
				modName = pack.modInfo.id();
			}

			this.pack = pack;
			this.text = SOURCE_BUILTIN_TEXT;
			this.tooltip = new TranslatableText("options.generic_value", SOURCE_BUILTIN_TEXT, modName);
		}

		@Override
		public Text decorate(Text description) {
			return new TranslatableText("pack.nameAndSource", description, this.text).formatted(Formatting.GRAY);
		}

		public Text getTooltip() {
			return this.tooltip;
		}
	}
}
