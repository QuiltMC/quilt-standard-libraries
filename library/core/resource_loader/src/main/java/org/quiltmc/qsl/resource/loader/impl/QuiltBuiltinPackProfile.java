/*
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

package org.quiltmc.qsl.resource.loader.impl;

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.SharedConstants;
import net.minecraft.resource.pack.PackCompatibility;
import net.minecraft.resource.pack.PackProfile;
import net.minecraft.resource.pack.PackSource;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import org.quiltmc.qsl.resource.loader.api.QuiltPackProfile;
import org.quiltmc.qsl.resource.loader.api.PackActivationType;

@ApiStatus.Internal
public final class QuiltBuiltinPackProfile extends PackProfile {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final ResourcePack pack;

	static @Nullable QuiltBuiltinPackProfile of(ModNioPack pack) {
		int version = SharedConstants.getGameVersion().getResourceVersion(pack.type);
		Info info = readInfoFromPack(pack.getName(), QuiltPackProfile.wrapToFactory(pack), version);

		if (info == null) {
			LOGGER.warn("Couldn't find pack meta for pack {}.", pack.getName());
			return null;
		}

		return new QuiltBuiltinPackProfile(pack, info);
	}

	private QuiltBuiltinPackProfile(ModNioPack pack, Info info) {
		super(
				pack.getName(),
                pack.getActivationType() == PackActivationType.ALWAYS_ENABLED,
				QuiltPackProfile.wrapToFactory(pack),
				pack.getDisplayName(),
				info,
				PackProfile.InsertionPosition.TOP,
				false,
				new BuiltinPackSource(pack)
		);
		this.pack = pack;
	}

	@Override
	public PackCompatibility getCompatibility() {
		// This is to ease multi-version mods whose built-in packs actually work across versions.
		return PackCompatibility.COMPATIBLE;
	}

	@Override
	public @NotNull PackActivationType getActivationType() {
		return this.pack.getActivationType();
	}

	/**
	 * Represents a built-in pack source.
	 * Similar to {@link PackSource#PACK_SOURCE_BUILTIN} but specifies the mod name too.
	 */
	public static class BuiltinPackSource implements PackSource {
		private static final Text SOURCE_BUILTIN_TEXT = Text.translatable("pack.source.builtin");
		private final ModNioPack pack;
		private final Text text;
		private final Text tooltip;

		BuiltinPackSource(ModNioPack pack) {
			String modName = pack.modInfo.name();

			if (modName == null) {
				modName = pack.modInfo.id();
			}

			this.pack = pack;
			this.text = SOURCE_BUILTIN_TEXT;
			this.tooltip = Text.translatable("options.generic_value", SOURCE_BUILTIN_TEXT, modName);
		}

		@Override
		public Text decorate(Text description) {
			return Text.translatable("pack.nameAndSource", description, this.text).formatted(Formatting.GRAY);
		}

		@Override
		public boolean shouldAddAutomatically() {
			return this.pack.getActivationType().isEnabledByDefault();
		}

		public Text getTooltip() {
			return this.tooltip;
		}
	}
}
